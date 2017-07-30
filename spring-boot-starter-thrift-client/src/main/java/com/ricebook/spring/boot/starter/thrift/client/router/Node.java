package com.ricebook.spring.boot.starter.thrift.client.router;

import lombok.Data;

@Data
public class Node {

  private int timeout;

  private String host;

  private int port;

}
