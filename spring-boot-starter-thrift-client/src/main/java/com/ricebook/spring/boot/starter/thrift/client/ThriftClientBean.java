package com.ricebook.spring.boot.starter.thrift.client;

import com.ricebook.spring.boot.starter.thrift.client.router.RouterAlgorithm;

import java.lang.reflect.Constructor;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
public class ThriftClientBean {

  private String name;

  private int timeout;

  private int retryTimes;

  private RouterAlgorithm router;

  private Constructor<?> clientConstructor;

}
