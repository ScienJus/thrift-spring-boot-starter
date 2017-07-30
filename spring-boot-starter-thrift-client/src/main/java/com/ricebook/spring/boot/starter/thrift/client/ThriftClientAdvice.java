package com.ricebook.spring.boot.starter.thrift.client;

import com.ricebook.spring.boot.starter.thrift.client.exception.NoAvailableTransportException;
import com.ricebook.spring.boot.starter.thrift.client.exception.ThriftApplicationException;
import com.ricebook.spring.boot.starter.thrift.client.exception.ThriftClientException;
import com.ricebook.spring.boot.starter.thrift.client.exception.ThriftClientOpenException;
import com.ricebook.spring.boot.starter.thrift.client.exception.ThriftClientRequestTimeoutException;
import com.ricebook.spring.boot.starter.thrift.client.router.Node;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dragon
 * @author ScienJus
 */
@Slf4j
@AllArgsConstructor
public class ThriftClientAdvice implements MethodInterceptor {
  
  private ThriftClientBean thriftClientBean;

  private GenericKeyedObjectPool<Node, TTransport> pool;

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    Object[] args = methodInvocation.getArguments();

    int times = 0;
    while (true) {
      if (++times == thriftClientBean.getRetryTimes()) {
        // handle all exceptions
        log.error(
            "All thrift client call failed, method is {}, args is {}, retryTimes: {}",
            methodInvocation.getMethod().getName(), args, times);
        throw new ThriftClientException("Thrift client call failed, bean name is " + thriftClientBean.getName());
      }

      Node node = thriftClientBean.getRouter().getTransportNode();
      if (node == null) {
        throw new NoAvailableTransportException("No available transport node", thriftClientBean.getName());
      }
      node.setTimeout(thriftClientBean.getTimeout());
      log.debug("Node info: ip is {}, port is {}, timeout is {}", node.getHost(), node.getPort(), node.getTimeout());

      TTransport transport = null;
      try {
        transport = pool.borrowObject(node);

        TProtocol protocol = new TBinaryProtocol(transport);
        Object client = thriftClientBean.getClientConstructor().newInstance(protocol);

        return ReflectionUtils.invokeMethod(methodInvocation.getMethod(), client, args);
      } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | NoSuchMethodException e) {
        throw new ThriftClientException("Thrift client call failed", e);
      } catch (UndeclaredThrowableException e) {
        Throwable undeclaredThrowable = e.getUndeclaredThrowable();
        if (undeclaredThrowable instanceof TTransportException) {
          TTransportException innerException = (TTransportException) e.getUndeclaredThrowable();
          Throwable realException = innerException.getCause();
          if (realException instanceof SocketTimeoutException) { // 超时,直接抛出异常,不进行重试
            if (transport != null) {
              transport.close();
            }
            log.error("Thrift client request timeout, ip is {}, port is {}, timeout is {}, method is {}, args is {}",
                node.getHost(), node.getPort(), node.getTimeout(),
                methodInvocation.getMethod(), args);
            throw new ThriftClientRequestTimeoutException("Thrift client request timeout", e);
          } else if (realException == null && innerException.getType() == TTransportException.END_OF_FILE) {
            // 服务端直接抛出了异常 or 服务端在被调用的过程中被关闭了
            pool.clear(node); // 把以前的对象池进行销毁
            if (transport != null) {
              transport.close();
            }
          } else if (realException instanceof SocketException) {
            pool.clear(node);
            if (transport != null) {
              transport.close();
            }
          }
        } else if (undeclaredThrowable instanceof TApplicationException) {  // 有可能服务端返回的结果里面存在 null
          log.error(
              "Thrift end of file, ip is {}, port is {}, timeout is {}, method is {}, args is {}",
              node.getHost(), node.getPort(), node.getTimeout(),
              methodInvocation.getMethod(), args);
          throw new ThriftApplicationException("Thrift end of file", e);
        } else if (undeclaredThrowable instanceof TException) { // idl exception
          throw undeclaredThrowable;
        }
        // unknown exception
        throw e;
      } catch (Exception e) {
        if (e instanceof ThriftClientOpenException) { // 创建连接失败
          Throwable realCause = e.getCause().getCause();
          // unreachable, reset router
          if(realCause instanceof SocketException && realCause.getMessage().contains("Network is unreachable")) {
            thriftClientBean.getRouter().reset();
          }
        }
      } finally {
        try {
          if (pool != null && transport != null) {
            pool.returnObject(node, transport);
          }
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }
}
