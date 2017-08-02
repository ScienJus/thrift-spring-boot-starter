package com.ricebook.spring.boot.starter.thrift.client;

import com.ricebook.spring.boot.starter.thrift.client.annotation.ThriftClient;
import com.ricebook.spring.boot.starter.thrift.client.exception.ThriftClientException;
import com.ricebook.spring.boot.starter.thrift.client.properties.ThriftClientProperties;
import com.ricebook.spring.boot.starter.thrift.client.properties.ThriftClientRoute;
import com.ricebook.spring.boot.starter.thrift.client.router.Node;
import com.ricebook.spring.boot.starter.thrift.client.router.RouterAlgorithmFactory;

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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftClientBeanPostProcessor implements BeanPostProcessor {

  private Map<String, ThriftClientBean> thriftClientMap = new ConcurrentHashMap<>();

  private GenericKeyedObjectPool<Node, TTransport> pool;

  private RouterAlgorithmFactory routerFactory;

  private ThriftClientProperties properties;

  public ThriftClientBeanPostProcessor(GenericKeyedObjectPool<Node, TTransport> pool,
      RouterAlgorithmFactory routerFactory, ThriftClientProperties properties) {
    this.pool = pool;
    this.routerFactory = routerFactory;
    this.properties = properties;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    Object target = getTargetBean(bean);
    Class clazz = target.getClass();

    ReflectionUtils.doWithFields(clazz, field -> {
      ThriftClient annotation = AnnotationUtils.getAnnotation(field, ThriftClient.class);
      ProxyFactory proxyFactory = createProxyFactory(annotation.value(), field.getName(), field.getType(), beanName, target);

      ReflectionUtils.makeAccessible(field);
      ReflectionUtils.setField(field, target, proxyFactory.getProxy());
    }, field -> AnnotationUtils.getAnnotation(field, ThriftClient.class) != null);

    ReflectionUtils.doWithMethods(clazz, method -> {
      Parameter parameter = method.getParameters()[0];

      ThriftClient annotation = AnnotationUtils.getAnnotation(method, ThriftClient.class);

      ThriftClientRoute route = getRoute(properties, annotation.value());

      ProxyFactory proxyFactory = createProxyFactory(annotation.value(), method.getName(), parameter.getType(), beanName, target);

      ReflectionUtils.makeAccessible(method);
      ReflectionUtils.invokeMethod(method, target, proxyFactory.getProxy());
    }, method ->
      method.getParameterCount() == 1 &&
          method.getReturnType() == Void.TYPE &&
          AnnotationUtils.getAnnotation(method, ThriftClient.class) != null
    );
    return bean;
  }

  private ThriftClientRoute getRoute(ThriftClientProperties properties, String name) {
    ThriftClientRoute route = properties.getRoutes().get(name);

    if (route == null) {
      throw new ThriftClientException("Can not found thrift client route, route name: " + name);
    }

    if (route.getRetryTimes() == null) {
      route.setRetryTimes(properties.getRetryTimes());
    }
    if (route.getTimeout() == null) {
      route.setTimeout(properties.getTimeout());
    }

    return route;
  }

  private ProxyFactory createProxyFactory(String thriftClientName, String fieldName, Class<?> type, String beanName, Object target) {
    String realClassName = getRealClassName(type);

    beanName = beanName + "-" + realClassName;

    ThriftClientBean thriftClientBean;
    try {
      thriftClientBean = createThriftClientBean(thriftClientName, type, beanName);
    } catch (SecurityException | NoSuchMethodException e) {
      throw new IllegalStateException("Can't create thrift client. class name is " + realClassName, e);
    }

    thriftClientMap.put(beanName, thriftClientBean);

    ProxyFactory proxyFactory = getProxyFactoryForThriftClient(target, type, fieldName);
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

  private ThriftClientBean createThriftClientBean(String name, Class<?> type, String beanName)
      throws NoSuchMethodException {
    ThriftClientBean thriftClientBean = new ThriftClientBean();

    thriftClientBean.setName(beanName);
    thriftClientBean.setRouter(routerFactory.createRouter(name));

    ThriftClientRoute route = properties.getRoutes().get(name);

    thriftClientBean.setTimeout(
        Optional.ofNullable(route.getTimeout())
            .orElse(properties.getTimeout()));
    thriftClientBean.setRetryTimes(
        Optional.ofNullable(route.getRetryTimes())
            .orElse(properties.getRetryTimes()));

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

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

  @PreDestroy
  public void destroy() {
    thriftClientMap.values().forEach(client -> client.getRouter().destroy());
  }

}
