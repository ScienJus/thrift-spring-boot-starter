package com.ricebook.spring.boot.starter.thrift.server;

import com.ricebook.spring.boot.starter.thrift.server.exception.ThriftServerException;
import com.ricebook.spring.boot.starter.thrift.server.properties.ThriftServerProperties;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dragon
 * @author ScienJus
 */
@Slf4j
public class THsHaServerArgs extends THsHaServer.Args {

  public THsHaServerArgs(String beanName, Object bean, ThriftServerProperties properties)
      throws TTransportException {
    super(new TNonblockingServerSocket(properties.getPort()));

    protocolFactory(new TBinaryProtocol.Factory());
    executorService(createInvokerPool(properties));
    try {
      processor(thriftProcessor(bean));
    } catch (Exception e) {
      throw new ThriftServerException("Can not create processor for " + beanName, e);
    }
  }

  private ExecutorService createInvokerPool(ThriftServerProperties properties) {
    // TODO: custom thread name
    return new ThreadPoolExecutor(
        properties.getMinWorker(),
        properties.getMaxWorker(),
        1, TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(properties.getWorkerQueueCapacity()));
  }

  private TProcessor thriftProcessor(Object bean) throws NoSuchMethodException {
    Class<?>[] handlerInterfaces = ClassUtils.getAllInterfaces(bean);

    Class<?> ifaceClass = Stream.of(handlerInterfaces)
        .filter(clazz -> clazz.getName().endsWith("$Iface"))
        .filter(iface -> iface.getDeclaringClass() != null)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No thrift Iface found on handler"));

    Class<TProcessor> processorClass = Stream.of(ifaceClass.getDeclaringClass().getDeclaredClasses())
        .filter(clazz ->clazz.getName().endsWith("$Processor"))
        .filter(TProcessor.class::isAssignableFrom)
        .findFirst()
        .map(processor -> (Class<TProcessor>) processor)
        .orElseThrow(() -> new IllegalStateException("No thrift Processor found on handler"));

    Constructor<TProcessor> processorConstructor = processorClass.getConstructor(ifaceClass);
    return BeanUtils.instantiateClass(processorConstructor, bean);
  }
}
