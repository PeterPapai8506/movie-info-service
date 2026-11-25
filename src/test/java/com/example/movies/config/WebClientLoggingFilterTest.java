package com.example.movies.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class WebClientLoggingFilterTest {
  private final ClientRequest request = ClientRequest
      .create(HttpMethod.GET, URI.create("https://example.test/movies"))
      .build();
  private final ExchangeFunction exchange = mock(ExchangeFunction.class);
  private final WebClientLoggingFilter filter = new WebClientLoggingFilter();

  @Test
  void forwardsResponse() {
    ClientResponse response = ClientResponse
        .create(HttpStatus.OK)
        .build();
    when(exchange.exchange(request)).thenReturn(Mono.just(response));
    StepVerifier
        .create(filter
            .create("OMDB")
            .filter(request, exchange))
        .expectNext(response)
        .verifyComplete();
  }

  @Test
  void propagatesExchangeError() {
    when(exchange.exchange(request)).thenReturn(Mono.error(new IllegalStateException("Unavailable")));
    StepVerifier
        .create(filter
            .create("OMDB")
            .filter(request, exchange))
        .expectError(IllegalStateException.class)
        .verify();
  }
}
