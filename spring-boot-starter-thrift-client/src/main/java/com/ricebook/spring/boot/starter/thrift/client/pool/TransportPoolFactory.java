package com.ricebook.spring.boot.starter.thrift.client.pool;

import com.ricebook.spring.boot.starter.thrift.client.exception.ThriftClientOpenException;
import com.ricebook.spring.boot.starter.thrift.client.router.Node;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * @author dragon
 * @author ScienJus
 */
public class TransportPoolFactory extends BaseKeyedPooledObjectFactory<Node, TTransport> {

  @Override
  public void destroyObject(Node key, PooledObject<TTransport> value) throws Exception {
    TTransport transport = value.getObject();
    if (transport.isOpen()) {
      transport.close();
    }
  }

  /**
   * 把对象放入对象池，返回是否安全的放入
   */
  @Override
  public boolean validateObject(Node key, PooledObject<TTransport> value) {
    try {
      TTransport transport = value.getObject();
      return transport.isOpen();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public TTransport create(Node node) {
    String host = node.getHost();
    int port = node.getPort();
    TTransport transport =
        new TFramedTransport(new TSocket(host, port, node.getTimeout()));
    try {
      transport.open();
    } catch (TTransportException e) {
      throw new ThriftClientOpenException("Connect to " + host + ":" + port + " failed", e);
    }
    return transport;
  }

  @Override
  public PooledObject<TTransport> wrap(TTransport value) {
    return new DefaultPooledObject<>(value);
  }

}
