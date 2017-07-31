package com.ricebook.spring.boot.starter.thrift.server.properties;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * @author dragon
 * @author ScienJus
 */
public class ThriftServerPropertiesCondition extends SpringBootCondition {
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context,
      AnnotatedTypeMetadata annotatedTypeMetadata) {
    RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment());
    Map<String, Object> properties = resolver.getSubProperties("thrift.server.");
    return new ConditionOutcome(!properties.isEmpty(), "Thrift Server Properties");
  }
}
