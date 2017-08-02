package com.ricebook.spring.boot.starter.thrift.client;

import com.ricebook.spring.boot.starter.thrift.client.pool.TransportPoolFactory;
import com.ricebook.spring.boot.starter.thrift.client.properties.ThriftClientProperties;
import com.ricebook.spring.boot.starter.thrift.client.router.DirectRouterAlgorithmFactory;
import com.ricebook.spring.boot.starter.thrift.client.router.Node;
import com.ricebook.spring.boot.starter.thrift.client.router.RouterAlgorithmFactory;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dragon
 * @author ScienJus
 */
@Configuration
@EnableConfigurationProperties(ThriftClientProperties.class)
public class ThriftClientAutoConfiguration {

  @Bean(destroyMethod = "close")
  @ConditionalOnMissingBean
  public GenericKeyedObjectPool<Node, TTransport> thriftClientsPool(ThriftClientProperties properties) {
    GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
    config.setJmxEnabled(false);  // cause spring will autodetect itself
    config.setMaxTotalPerKey(properties.getPoolMaxTotalPerKey());
    config.setMaxIdlePerKey(properties.getPoolMaxIdlePerKey());
    config.setMinIdlePerKey(properties.getPoolMinIdlePerKey());
    config.setMaxWaitMillis(properties.getPoolMaxWait());
    // TODO fixbug设置每次还回对象的时候，进行对象的正确性判断
    config.setTestOnReturn(true);
    TransportPoolFactory poolFactory = new TransportPoolFactory();
    return new GenericKeyedObjectPool<>(poolFactory, config);
  }

  @Bean
  public ThriftClientBeanPostProcessor thriftClientBeanPostProcessor(
      GenericKeyedObjectPool<Node, TTransport> pool,
      RouterAlgorithmFactory routerFactory,
      ThriftClientProperties properties) {
    return new ThriftClientBeanPostProcessor(pool, routerFactory, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public RouterAlgorithmFactory routerFactory(ThriftClientProperties properties) {
    return new DirectRouterAlgorithmFactory(properties);
  }
}
