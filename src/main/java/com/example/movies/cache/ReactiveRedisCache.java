package com.example.movies.cache;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveRedisCache {

  private final RedisTemplateFactory redisTemplateFactory;
  private final RedisOperationExecutor redisOperationExecutor;

  public <T> Mono<T> getOrLoad(String key, Class<T> type, Duration ttl, Supplier<Mono<T>> loader) {
    return getOrLoad(key, type, ttl, loader, value -> true);
  }

  public <T> Mono<T> getOrLoad(String key, Class<T> type, Duration ttl, Supplier<Mono<T>> loader, Predicate<T> cacheable) {
    return read(key, type).switchIfEmpty(Mono.defer(() -> {
      log.debug("Cache MISS or unavailable: {}", key);

      return loader
          .get()
          .flatMap(value -> cacheable.test(value) ? save(key, value, type, ttl).thenReturn(value) : Mono.just(value));
    }));
  }

  private <T> Mono<T> read(String key, Class<T> type) {
    return redisOperationExecutor
        .execute(() -> redisTemplateFactory
            .getTemplate(type)
            .opsForValue()
            .get(key))
        .doOnNext(value -> log.debug("Cache HIT: {}", key));
  }

  private <T> Mono<Void> save(String key, T value, Class<T> type, Duration ttl) {
    return redisOperationExecutor
        .execute(() -> redisTemplateFactory
            .getTemplate(type)
            .opsForValue()
            .set(key, value, ttl))
        .then();
  }
}