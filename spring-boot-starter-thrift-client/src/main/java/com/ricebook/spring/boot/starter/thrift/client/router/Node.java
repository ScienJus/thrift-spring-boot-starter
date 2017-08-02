package com.ricebook.spring.boot.starter.thrift.client.router;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
public class Node {

  private int timeout;

  private String host;

  private int port;

}
