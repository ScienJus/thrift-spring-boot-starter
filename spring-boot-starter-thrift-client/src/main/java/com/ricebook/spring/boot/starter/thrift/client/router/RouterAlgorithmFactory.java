package com.ricebook.spring.boot.starter.thrift.client.router;

/**
 * @author dragon
 * @author ScienJus
 */
public interface RouterAlgorithmFactory {

  RouterAlgorithm createRouter(String name);
}
