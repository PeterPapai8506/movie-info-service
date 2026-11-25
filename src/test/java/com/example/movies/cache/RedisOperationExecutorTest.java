package com.example.movies.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.example.movies.config.MovieProperties;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RedisOperationExecutorTest {
  @Test
  void timesOutSlowOperation() {
    MovieProperties props = mock(MovieProperties.class);
    when(props.cache()).thenReturn(new MovieProperties.Cache(Duration.ofHours(24), Duration.ofMillis(500)));
    RedisOperationExecutor executor = new RedisOperationExecutor(props);
    StepVerifier
        .withVirtualTime(() -> executor.execute(Mono::never))
        .thenAwait(Duration.ofMillis(500))
        .expectError(TimeoutException.class)
        .verify();
  }
}
