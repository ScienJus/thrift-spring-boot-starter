package com.ricebook.spring.boot.starter.thrift.server.exception;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftServerException extends RuntimeException {

  public ThriftServerException(String message) {
    super(message);
  }

  public ThriftServerException(String message, Throwable t) {
    super(message, t);
  }
}
