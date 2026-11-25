package com.example.movies.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.movies.config.MovieProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveRedisCacheTest {
  private static final String KEY = "movie:test";
  private static final Duration TTL = Duration.ofHours(24);
  private final RedisTemplateFactory factory = mock(RedisTemplateFactory.class);
  private final MovieProperties props = mock(MovieProperties.class);
  private final ReactiveRedisCache cache = new ReactiveRedisCache(factory, new RedisOperationExecutor(props));
  private ReactiveValueOperations<String, String> operations;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    ReactiveRedisTemplate<String, String> template = mock(ReactiveRedisTemplate.class);
    operations = mock(ReactiveValueOperations.class);
    when(factory.getTemplate(String.class)).thenReturn(template);
    when(template.opsForValue()).thenReturn(operations);
    when(props.cache()).thenReturn(new MovieProperties.Cache(TTL, Duration.ofSeconds(5)));
  }

  @Test
  void returnsCachedValueWithoutLoading() {
    when(operations.get(KEY)).thenReturn(Mono.just("cached"));
    StepVerifier
        .create(cache.getOrLoad(KEY, String.class, TTL, () -> Mono.error(new AssertionError("Unexpected load"))))
        .expectNext("cached")
        .verifyComplete();
    verify(operations, never()).set(KEY, "cached", TTL);
  }

  @Test
  void loadsAndStoresMissingValue() {
    when(operations.get(KEY)).thenReturn(Mono.empty());
    when(operations.set(KEY, "loaded", TTL)).thenReturn(Mono.just(true));
    StepVerifier
        .create(cache.getOrLoad(KEY, String.class, TTL, () -> Mono.just("loaded")))
        .expectNext("loaded")
        .verifyComplete();
    verify(operations).set(KEY, "loaded", TTL);
  }

  @Test
  void skipsNonCacheableValue() {
    when(operations.get(KEY)).thenReturn(Mono.empty());
    StepVerifier
        .create(cache.getOrLoad(KEY, String.class, TTL, () -> Mono.just("loaded"), value -> false))
        .expectNext("loaded")
        .verifyComplete();
    verify(operations, never()).set(KEY, "loaded", TTL);
  }

  @Test
  void propagatesLoaderFailure() {
    when(operations.get(KEY)).thenReturn(Mono.empty());
    StepVerifier
        .create(cache.getOrLoad(KEY, String.class, TTL, () -> Mono.<String>error(new IllegalStateException("API unavailable"))))
        .expectError(IllegalStateException.class)
        .verify();
  }
}
