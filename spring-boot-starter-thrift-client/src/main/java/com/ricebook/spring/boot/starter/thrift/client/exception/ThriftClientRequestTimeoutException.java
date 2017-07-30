package com.ricebook.spring.boot.starter.thrift.client.exception;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftClientRequestTimeoutException extends RuntimeException {

  public ThriftClientRequestTimeoutException(String message) {
    super(message);
  }

  public ThriftClientRequestTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
