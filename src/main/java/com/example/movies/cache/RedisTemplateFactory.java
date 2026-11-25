package com.example.movies.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTemplateFactory {
  private final ReactiveRedisConnectionFactory connectionFactory;
  private final Map<Class<?>, ReactiveRedisTemplate<?, ?>> templates = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  public <T> ReactiveRedisTemplate<String, T> getTemplate(Class<T> type) {
    return (ReactiveRedisTemplate<String, T>) templates.computeIfAbsent(type, ignored -> createTemplate(type));
  }

  private <T> ReactiveRedisTemplate<String, T> createTemplate(Class<T> type) {
    RedisSerializationContext<String, T> context = RedisSerializationContext
        .<String, T>newSerializationContext(new StringRedisSerializer())
        .value(new JacksonJsonRedisSerializer<>(type))
        .build();

    return new ReactiveRedisTemplate<>(connectionFactory, context);
  }
}
