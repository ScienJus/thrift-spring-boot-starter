package com.ricebook.spring.boot.starter.thrift.client.exception;

/**
 * @author dragon
 * @author ScienJus
 */
public class NoAvailableTransportException extends Exception {

  public NoAvailableTransportException(String message, String beanName) {
    this(message, beanName, null);
  }

  public NoAvailableTransportException(String message, String beanName, Throwable cause) {
    super(message + " bean name is " + beanName, cause);
  }
}
