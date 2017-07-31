package com.ricebook.spring.boot.starter.thrift.server;

import com.ricebook.spring.boot.starter.thrift.server.annotation.ThriftService;
import com.ricebook.spring.boot.starter.thrift.server.exception.ThriftServerException;
import com.ricebook.spring.boot.starter.thrift.server.properties.MultipleThriftServerProperties;
import com.ricebook.spring.boot.starter.thrift.server.properties.ThriftServerProperties;
import com.ricebook.spring.boot.starter.thrift.server.properties.ThriftServerPropertiesCondition;

import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dragon
 * @author ScienJus
 */
@Slf4j
@Configuration
@Conditional(ThriftServerPropertiesCondition.class)
@EnableConfigurationProperties(MultipleThriftServerProperties.class)
public class ThriftServerAutoConfiguration implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Bean
  public TServerGroup thriftServerGroup(MultipleThriftServerProperties multipleThriftServerProperties) {
    String[] beanNames = applicationContext.getBeanNamesForAnnotation(ThriftService.class);
    if (beanNames == null || beanNames.length == 0) {
      throw new ThriftServerException("Can not found any thrift service");
    }

    List<TServer> servers = Stream.of(beanNames).map(beanName -> {
      Object bean = applicationContext.getBean(beanName);
      ThriftService thriftService = bean.getClass().getAnnotation(ThriftService.class);
      String serviceName = thriftService.value();
      ThriftServerProperties properties = multipleThriftServerProperties.getServer()
          .get(serviceName);

      if (properties == null) {
        throw new ThriftServerException("Can not found thrift server properties, service name: " + serviceName);
      }

      THsHaServer.Args args;
        try {
        args = new THsHaServerArgs(beanName, bean, properties);
      } catch (Exception e) {
        throw new ThriftServerException("Can not create server for " + beanName, e);
      }

      log.info("Thrift server is starting, service name is {}, port is {}, minWorker is {}, maxWorker is {}",
          beanName, properties.getPort(), properties.getMinWorker(), properties.getMaxWorker());
      return new THsHaServer(args);
    }).collect(Collectors.toList());

    return new TServerGroup(servers);
  }

  @Bean
  public ThriftServerBootstrap thriftServerBootstrap(TServerGroup thriftServerGroup) {
    return new ThriftServerBootstrap(thriftServerGroup);
  }
}
