package com.example.movies.cache;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

class RedisTemplateFactoryTest {
  private final RedisTemplateFactory factory = new RedisTemplateFactory(mock(ReactiveRedisConnectionFactory.class));

  @Test
  void reusesTemplateForSameType() {
    assertSame(factory.getTemplate(String.class), factory.getTemplate(String.class));
  }

  @Test
  void createsSeparateTemplatesForDifferentTypes() {
    ReactiveRedisTemplate<String, ?> stringTemplate = factory.getTemplate(String.class);
    ReactiveRedisTemplate<String, ?> integerTemplate = factory.getTemplate(Integer.class);
    assertNotSame(stringTemplate, integerTemplate);
  }
}
