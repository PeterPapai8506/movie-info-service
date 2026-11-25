package com.example.movies.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebClientLoggingFilter {

  public ExchangeFilterFunction create(String provider) {
    return (request, next) -> Mono.defer(() -> {
      long start = System.nanoTime();

      return next
          .exchange(request)
          .doOnNext(response -> log.info("{} {} {} status={} responseHeadersDuration={}ms", provider, request.method(), request
              .url()
              .getPath(), response
              .statusCode()
              .value(), elapsedMillis(start)))
          .doOnError(error -> log.warn("{} {} {} failed duration={}ms: {}", provider, request.method(), request
              .url()
              .getPath(), elapsedMillis(start), error
              .getClass()
              .getSimpleName()));
    });
  }

  private long elapsedMillis(long start) {
    return Duration
        .ofNanos(System.nanoTime() - start)
        .toMillis();
  }
}
