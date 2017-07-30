package com.ricebook.spring.boot.starter.thrift.client.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
@ConfigurationProperties(prefix = "thrift.client")
public class ThriftClientProperties {

  private String address;

  private int timeout;

  private int retryTimes;

  private int poolMaxTotalPerKey = 200;

  private int poolMaxIdlePerKey = 40;

  private int poolMinIdlePerKey = 10;

  private long poolMaxWait = 1000;

}
