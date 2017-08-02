package com.ricebook.spring.boot.starter.thrift.client.properties;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
public class ThriftClientRoute {

  private String address;

  private Integer timeout;

  private Integer retryTimes;

}
