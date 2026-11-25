package com.example.movies.provider;

import static com.example.movies.util.ApiFixtures.read;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.movies.config.MovieProperties;
import com.example.movies.dto.Movie;
import com.example.movies.external.api.client.TmdbApiClient;
import com.example.movies.external.api.dto.TmdbCredits;
import com.example.movies.external.api.dto.TmdbCredits.CrewMember;
import com.example.movies.external.api.dto.TmdbMovieHit;
import com.example.movies.external.api.dto.TmdbSearchResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TmdbProviderTest {
  private final TmdbApiClient client = mock(TmdbApiClient.class);
  private final MovieProperties props = mock(MovieProperties.class);
  private final TmdbProvider provider = new TmdbProvider(client, props);

  @BeforeEach
  void setUp() {
    when(props.detailConcurrency()).thenReturn(1);
  }

  @Test
  void extractsYearAndDistinctDirectors() {
    TmdbSearchResponse response = read("tmdb/search-1.json", TmdbSearchResponse.class);
    TmdbMovieHit hit = response
        .results()
        .getFirst();
    TmdbCredits credits = read("tmdb/credits-" + hit.id() + ".json", TmdbCredits.class);
    when(props.maxResults()).thenReturn(1);
    when(client.search("Batman", 1)).thenReturn(Mono.just(response));
    when(client.credits(hit.id())).thenReturn(Mono.just(credits));
    StepVerifier
        .create(provider.search("Batman"))
        .expectNextMatches(movie -> movie
            .title()
            .equals(hit.title()) && movie
            .year()
            .equals(hit
                .releaseDate()
                .substring(0, 4)) && movie
            .directors()
            .equals(credits
                .crew()
                .stream()
                .filter(member -> "Director".equalsIgnoreCase(member.job()))
                .map(CrewMember::name)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList()))
        .verifyComplete();
    verify(client).credits(hit.id());
  }

  @Test
  void propagatesTransportFailure() {
    when(client.search("Batman", 1)).thenReturn(Mono.error(new IllegalStateException("Unavailable")));
    StepVerifier
        .create(provider.search("Batman"))
        .expectError(IllegalStateException.class)
        .verify();
  }

  @Test
  void loadsNextPageWhenLimitAllows() {
    when(props.detailConcurrency()).thenReturn(1);
    when(client.search("Batman", 1)).thenReturn(Mono.just(new TmdbSearchResponse(2, List.of(new TmdbMovieHit(1, "Batman", "1989")))));
    when(client.search("Batman", 2)).thenReturn(Mono.just(new TmdbSearchResponse(2, List.of(new TmdbMovieHit(2, "Batman Returns", "1992")))));
    when(client.credits(1)).thenReturn(Mono.just(new TmdbCredits(null)));
    when(client.credits(2)).thenReturn(Mono.just(new TmdbCredits(List.of())));
    StepVerifier
        .create(provider.search("Batman"))
        .expectNext(new Movie("Batman", "1989", List.of()), new Movie("Batman Returns", "1992", List.of()))
        .verifyComplete();
    verify(client).search("Batman", 2);
  }

  @Test
  void stopsPaginationAtLimit() {
    when(props.maxResults()).thenReturn(1);
    when(props.detailConcurrency()).thenReturn(1);
    when(client.search("Batman", 1)).thenReturn(Mono.just(new TmdbSearchResponse(2, List.of(new TmdbMovieHit(1, "Batman", null)))));
    when(client.credits(1)).thenReturn(Mono.just(new TmdbCredits(List.of())));
    StepVerifier
        .create(provider.search("Batman"))
        .expectNext(new Movie("Batman", "", List.of()))
        .verifyComplete();
    verify(client, never()).search("Batman", 2);
  }
}
