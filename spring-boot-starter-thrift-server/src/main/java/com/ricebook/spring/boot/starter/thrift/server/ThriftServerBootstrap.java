package com.ricebook.spring.boot.starter.thrift.server;

import org.apache.thrift.server.TServer;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dragon
 * @author ScienJus
 */
@Slf4j
@AllArgsConstructor
public class ThriftServerBootstrap implements SmartLifecycle {

  private TServerGroup thriftServerGroup;

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    if (isRunning()) {
      log.info("Shutting down thrift servers");
      thriftServerGroup.getServers().forEach(server -> {
        server.setShouldStop(true);
        server.stop();
      });
      if (runnable != null) {
        runnable.run();
      }
    }
  }

  @Override
  public void start() {
    if (CollectionUtils.isEmpty(thriftServerGroup.getServers())) {
      return;
    }
    log.info("Starting thrift servers");
    AtomicInteger serverIndex = new AtomicInteger(0);
    thriftServerGroup.getServers().forEach(server -> {
      ThriftRunner runner = new ThriftRunner(server);
      new Thread(runner, "thrift-server-" + serverIndex.incrementAndGet()).start();
    });
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public boolean isRunning() {
    return thriftServerGroup.getServers().stream()
        .anyMatch(TServer::isServing);
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE;
  }

  private static class ThriftRunner implements Runnable {

    private TServer server;

    ThriftRunner(TServer server) {
      this.server = server;
    }

    @Override
    public void run() {
      if (server != null) {
        this.server.serve();
      }
    }
  }

}
