package com.ricebook.spring.boot.starter.thrift.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

import lombok.Data;

/**
 * @author dragon
 * @author ScienJus
 */
@Data
@ConfigurationProperties(prefix = "thrift")
public class MultipleThriftServerProperties {

  Map<String, ThriftServerProperties> server;

}
