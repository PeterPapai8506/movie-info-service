package com.example.movies.cache;

import com.example.movies.config.MovieProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOperationExecutor {

  private final MovieProperties props;

  @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
  public <T> Mono<T> execute(Supplier<Mono<T>> operation) {
    return Mono
        .defer(operation)
        .timeout(props
            .cache()
            .operationTimeout());
  }

  private <T> Mono<T> fallback(Throwable error) {
    log.debug("Redis operation failed or unavailable", error);
    return Mono.empty();
  }
}
