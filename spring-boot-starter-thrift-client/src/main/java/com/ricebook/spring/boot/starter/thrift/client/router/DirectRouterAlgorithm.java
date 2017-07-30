package com.ricebook.spring.boot.starter.thrift.client.router;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectRouterAlgorithm implements RouterAlgorithm {

  private final String[] directAddress;

  private static final int MAX_INDEX = Integer.MAX_VALUE - 100000;

  private AtomicInteger currentIndex = new AtomicInteger(0);

  private List<Node> nodes = new CopyOnWriteArrayList<>();

  public DirectRouterAlgorithm(String directAddress) {
    this.directAddress = directAddress.split(",");
    init();
  }

  @Override
  public void init() {
    nodes = Stream.of(directAddress)
        .map(address -> {
          String[] hostAndPort = address.split(":");
          String host = hostAndPort[0];
          int port = Integer.valueOf(hostAndPort[1]);
          Node node = new Node();
          node.setHost(host);
          node.setPort(port);
          return node;
        })
        .collect(Collectors.toList());
  }

  @Override
  public synchronized Node getTransportNode() {
    if(nodes.isEmpty()) {
      return null;
    }
    int i = currentIndex.getAndIncrement();
    if (i > MAX_INDEX) {
      currentIndex.set(0);
    }
    return nodes.get(i % nodes.size());
  }

  @Override
  public void reset() {
    init();
  }

  @Override
  public void destroy() {
    // do nothing
  }
}
