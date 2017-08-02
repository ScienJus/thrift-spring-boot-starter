package com.ricebook.spring.boot.starter.thrift.client.router;

/**
 * @author dragon
 * @author ScienJus
 */
public interface RouterAlgorithm {

  void init();

  Node getTransportNode();

  void reset();

  void destroy();

}
