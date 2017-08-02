package com.ricebook.spring.boot.starter.thrift.server.properties;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
public class ThriftServerRegistry {

  // 服务使用的端口
  private int port;

  // 服务进程的工作队列最小值
  private Integer minWorker;

  // 服务进程的工作队列最大值
  private Integer maxWorker;

  // 工作队列长度
  private Integer workerQueueCapacity;

}
