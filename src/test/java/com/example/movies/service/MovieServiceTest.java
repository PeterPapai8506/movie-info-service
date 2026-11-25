package com.example.movies.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import com.example.movies.config.MovieProperties;
import com.example.movies.dto.ApiName;
import com.example.movies.dto.Movie;
import com.example.movies.exception.UnsupportedApiException;
import com.example.movies.provider.MovieProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class MovieServiceTest {
  private final MovieProvider provider = mock(MovieProvider.class);
  private final SearchRecorder recorder = mock(SearchRecorder.class);
  private final MovieProperties props = mock(MovieProperties.class);
  private final MovieService service = new MovieService(Map.of(ApiName.OMDB, provider), recorder, props);

  @Test
  void normalizesTitleAndLimitsResults() {
    Movie movie = new Movie("Batman", "1989", List.of("Tim Burton"));
    when(props.maxResults()).thenReturn(1);
    when(provider.search("Batman")).thenReturn(Flux.just(movie, movie));
    StepVerifier
        .create(service.find(" Batman ", ApiName.OMDB))
        .expectNext(movie)
        .verifyComplete();
    verify(recorder).record("Batman", ApiName.OMDB);
    verify(provider).search("Batman");
  }

  @Test
  void rejectsUnregisteredProvider() {
    StepVerifier
        .create(service.find("Batman", ApiName.TMDB))
        .expectError(UnsupportedApiException.class)
        .verify();
    verifyNoInteractions(recorder, provider);
  }
}
