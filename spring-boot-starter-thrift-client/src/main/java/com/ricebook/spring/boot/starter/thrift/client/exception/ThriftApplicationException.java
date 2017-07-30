package com.ricebook.spring.boot.starter.thrift.client.exception;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftApplicationException extends RuntimeException {

  public ThriftApplicationException(String message) {
    super(message);
  }

  public ThriftApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
