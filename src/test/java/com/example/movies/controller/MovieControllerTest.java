package com.example.movies.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import com.example.movies.dto.ApiName;
import com.example.movies.dto.Movie;
import com.example.movies.dto.MovieResponse;
import com.example.movies.exception.UnsupportedApiException;
import com.example.movies.service.MovieService;
import com.example.movies.util.ReactiveJsonArrayTemplateEncoder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class MovieControllerTest {
  private final MovieService service = mock(MovieService.class);
  private final ReactiveJsonArrayTemplateEncoder encoder = mock(ReactiveJsonArrayTemplateEncoder.class);
  private final MovieController controller = new MovieController(service, encoder);

  @Test
  void delegatesToServiceAndEncoderOnSubscription() {
    Flux<Movie> movies = Flux.just(new Movie("Batman", "1989", List.of()));
    DataBuffer buffer = DefaultDataBufferFactory.sharedInstance.wrap(new byte[]{1});
    when(service.find("Batman", ApiName.OMDB)).thenReturn(movies);
    when(encoder.encode(new MovieResponse(List.of()), movies)).thenReturn(Flux.just(buffer));
    Flux<DataBuffer> result = controller.movies("Batman", "OMDB");
    verifyNoInteractions(service, encoder);
    StepVerifier
        .create(result)
        .expectNext(buffer)
        .verifyComplete();
  }

  @Test
  void rejectsInvalidProviderBeforeDelegating() {
    StepVerifier
        .create(controller.movies("Batman", "invalid"))
        .expectError(UnsupportedApiException.class)
        .verify();
    verifyNoInteractions(service, encoder);
  }
}
