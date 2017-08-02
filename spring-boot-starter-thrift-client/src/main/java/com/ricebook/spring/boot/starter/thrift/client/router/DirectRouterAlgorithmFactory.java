package com.ricebook.spring.boot.starter.thrift.client.router;

import com.ricebook.spring.boot.starter.thrift.client.properties.ThriftClientProperties;
import com.ricebook.spring.boot.starter.thrift.client.properties.ThriftClientRoute;

import java.util.Map;

/**
 * @author dragon
 * @author ScienJus
 */
public class DirectRouterAlgorithmFactory implements RouterAlgorithmFactory {

  private Map<String, ThriftClientRoute> routes;

  public DirectRouterAlgorithmFactory(ThriftClientProperties properties) {
    this.routes = properties.getRoutes();
  }

  @Override
  public RouterAlgorithm createRouter(String name) {
    return new DirectRouterAlgorithm(routes.get(name).getAddress());
  }
}
