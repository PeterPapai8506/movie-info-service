package com.example.movies.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.movies.dto.Movie;
import com.example.movies.dto.MovieResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class ReactiveJsonArrayTemplateEncoderTest {
  private final ObjectMapper mapper = JsonMapper
      .builder()
      .build();
  private final ReactiveJsonArrayTemplateEncoder encoder = new ReactiveJsonArrayTemplateEncoder(mapper);

  @Test
  void encodesEmptyArray() {
    StepVerifier
        .create(encode(Flux.empty()))
        .assertNext(json -> assertEquals(mapper.readTree("{\"movies\":[]}"), mapper.readTree(json)))
        .verifyComplete();
  }

  @Test
  void preservesUnicodeAndClosesPartialResponse() {
    Movie movie = new Movie("Am\u00e9lie", "2001", List.of("Jean-Pierre Jeunet"));
    Flux<Movie> items = Flux.concat(Flux.just(movie), Flux.error(new IllegalStateException("Provider unavailable")));
    StepVerifier
        .create(encode(items))
        .assertNext(json -> assertEquals(mapper.readTree(mapper.writeValueAsString(new MovieResponse(List.of(movie)))), mapper.readTree(json)))
        .verifyComplete();
  }

  @Test
  void propagatesInitialError() {
    StepVerifier
        .create(encoder.encode(new MovieResponse(List.of()), Flux.error(new IllegalStateException("Provider unavailable"))))
        .expectError(IllegalStateException.class)
        .verify();
  }

  private Mono<String> encode(Flux<Movie> items) {
    return encoder
        .encode(new MovieResponse(List.of()), items)
        .map(this::decode)
        .collectList()
        .map(parts -> String.join("", parts));
  }

  private String decode(DataBuffer buffer) {
    try {
      return buffer.toString(StandardCharsets.UTF_8);
    } finally {
      DataBufferUtils.release(buffer);
    }
  }
}
