package com.ricebook.spring.boot.starter.thrift.client.router;

/**
 * Created by dragon on 16/5/6.
 */
public interface RouterAlgorithm {

  void init();

  Node getTransportNode();

  void reset();

  void destroy();

}
