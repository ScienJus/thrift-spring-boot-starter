package com.ricebook.spring.boot.starter.thrift.client.exception;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftClientOpenException extends RuntimeException {

  public ThriftClientOpenException(String message) {
    super(message);
  }

  public ThriftClientOpenException(String message, Throwable cause) {
    super(message, cause);
  }
}
