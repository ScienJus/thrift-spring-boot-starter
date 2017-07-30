package com.ricebook.spring.boot.starter.thrift.client.exception;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftClientException extends RuntimeException {

  public ThriftClientException(String message) {
    super(message);
  }

  public ThriftClientException(String message, Throwable t) {
    super(message, t);
  }
}
