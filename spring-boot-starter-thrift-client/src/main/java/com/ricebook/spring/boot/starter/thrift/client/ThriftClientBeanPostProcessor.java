package com.ricebook.spring.boot.starter.thrift.client;

import com.ricebook.spring.boot.starter.thrift.client.annotation.ThriftClient;
import com.ricebook.spring.boot.starter.thrift.client.properties.ThriftClientProperties;
import com.ricebook.spring.boot.starter.thrift.client.router.Node;
import com.ricebook.spring.boot.starter.thrift.client.router.RouterAlgorithm;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftClientBeanPostProcessor implements BeanPostProcessor {

  private Map<String, Class> beansToProcess = new HashMap<>();

  private Map<String, ThriftClientBean> thriftClientMap = new ConcurrentHashMap<>();

  private GenericKeyedObjectPool<Node, TTransport> pool;

  private RouterAlgorithm router;

  private ThriftClientProperties properties;

  public ThriftClientBeanPostProcessor(GenericKeyedObjectPool<Node, TTransport> pool,
      RouterAlgorithm router, ThriftClientProperties properties) {
    this.pool = pool;
    this.router = router;
    this.properties = properties;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    Class clazz = bean.getClass();
    do {
      if (Stream.of(clazz.getDeclaredFields())
          .anyMatch(field -> field.isAnnotationPresent(ThriftClient.class))) {
        beansToProcess.put(beanName, clazz);
      }
      if (!beansToProcess.containsKey(beanName)) {
        if (Stream.of(clazz.getDeclaredMethods())
            .anyMatch(method -> method.isAnnotationPresent(ThriftClient.class)
                && method.getParameterCount() == 1)) {
          beansToProcess.put(beanName, clazz);
        }
      }
      clazz = clazz.getSuperclass();
    } while (clazz != null);
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (!beansToProcess.containsKey(beanName)) {
      return bean;
    }
    Object target = getTargetBean(bean);
    Class clazz = beansToProcess.get(beanName);

    Stream.of(clazz.getDeclaredFields())
        .filter(field -> AnnotationUtils.getAnnotation(field, ThriftClient.class) != null)
        .forEach(field -> {
          ProxyFactory proxyFactory = createProxyFactory(field.getName(), field.getType(), beanName, target);

          ReflectionUtils.makeAccessible(field);
          ReflectionUtils.setField(field, target, proxyFactory.getProxy());
        });

    Stream.of(clazz.getDeclaredMethods())
        .filter(method -> method.getParameterCount() == 1)
        .filter(method -> method.getReturnType().equals(Void.TYPE))
        .filter(method -> AnnotationUtils.getAnnotation(method, ThriftClient.class) != null)
        .forEach(method -> {
          Parameter parameter = method.getParameters()[0];

          ProxyFactory proxyFactory = createProxyFactory(method.getName(), parameter.getType(), beanName, target);

          ReflectionUtils.makeAccessible(method);
          ReflectionUtils.invokeMethod(method, target, proxyFactory.getProxy());
        });

    return bean;
  }

  private ProxyFactory createProxyFactory(String name, Class<?> type, String beanName, Object target) {
    String realClassName = getRealClassName(type);

    beanName = beanName + "-" + realClassName;

    ThriftClientBean thriftClientBean;
    try {
      thriftClientBean = createThriftClientBean(type, beanName);
    } catch (SecurityException | NoSuchMethodException e) {
      throw new IllegalStateException("Can't create thrift client. class name is " + realClassName, e);
    }

    thriftClientMap.put(beanName, thriftClientBean);

    ProxyFactory proxyFactory = getProxyFactoryForThriftClient(target, type, name);
    proxyFactory.addAdvice(new ThriftClientAdvice(thriftClientBean, pool));

    proxyFactory.setFrozen(true);
    proxyFactory.setProxyTargetClass(true);

    return proxyFactory;
  }

  private static String getRealClassName(Class<?> clazz) {
    String className = clazz.getCanonicalName();
    int lastComma = className.lastIndexOf(".");
    return className.substring(0, lastComma);
  }

  private ThriftClientBean createThriftClientBean(Class<?> type, String beanName)
      throws NoSuchMethodException {
    ThriftClientBean thriftClientBean = new ThriftClientBean();

    thriftClientBean.setName(beanName);
    thriftClientBean.setRouter(router);
    thriftClientBean.setTimeout(properties.getTimeout());
    thriftClientBean.setRetryTimes(properties.getRetryTimes());

    Constructor<?> clientConstructor = type.getConstructor(TProtocol.class);
    thriftClientBean.setClientConstructor(clientConstructor);

    return thriftClientBean;
  }

  private static Object getTargetBean(Object bean) {
    Object target = bean;
    try {
      while (AopUtils.isAopProxy(target)) {
        target = ((Advised) target).getTargetSource().getTarget();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Get target bean error", e);
    }
    return target;
  }

  private ProxyFactory getProxyFactoryForThriftClient(Object bean, Class<?> type, String name) {
    ProxyFactory proxyFactory;
    try {
      proxyFactory = new ProxyFactory(
          BeanUtils.instantiateClass(type.getConstructor(TProtocol.class), (TProtocol) null));
    } catch (NoSuchMethodException e) {
      throw new InvalidPropertyException(bean.getClass(), name, e.getMessage());
    }
    return proxyFactory;
  }

  @PreDestroy
  public void destroy() {
    thriftClientMap.values().forEach(client -> client.getRouter().destroy());
  }

}
