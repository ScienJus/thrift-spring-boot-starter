package com.ricebook.spring.boot.starter.thrift.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
@ConfigurationProperties(prefix = "thrift.server")
public class ThriftServerProperties {

  Map<String, ThriftServerRegistry> registries;

  // 服务进程的工作队列最小值
  private int minWorker = Runtime.getRuntime().availableProcessors();

  // 服务进程的工作队列最大值
  private int maxWorker = Runtime.getRuntime().availableProcessors();

  // 工作队列长度
  private int workerQueueCapacity = 1024;

}
