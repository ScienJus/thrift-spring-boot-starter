package com.ricebook.spring.boot.starter.thrift.client.properties;

import lombok.Data;

/**
 * @author xieenlong
 * @date 17/8/2.
 */
@Data
public class ThriftClientRoute {

  private String address;

  private int timeout;

  private int retryTimes;

  private int poolMaxTotalPerKey;

  private int poolMaxIdlePerKey;

  private int poolMinIdlePerKey;

  private long poolMaxWait;

}
