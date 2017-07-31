package com.ricebook.spring.boot.starter.thrift.server;

import org.apache.thrift.server.TServer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TServerGroup {

  private List<TServer> servers;

}
