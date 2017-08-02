package com.ricebook.spring.boot.starter.thrift.client.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
@ConfigurationProperties(prefix = "thrift.client")
public class ThriftClientProperties {

  private Map<String, ThriftClientRoute> routes;

  private int timeout = 500;

  private int retryTimes = 3;

  private int poolMaxTotalPerKey = 200;

  private int poolMaxIdlePerKey = 40;

  private int poolMinIdlePerKey = 10;

  private long poolMaxWait = 1000;

}
