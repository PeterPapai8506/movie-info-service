package com.example.movies.provider;

import static com.example.movies.util.ApiFixtures.read;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.movies.config.MovieProperties;
import com.example.movies.dto.Movie;
import com.example.movies.exception.ProviderException;
import com.example.movies.external.api.client.OmdbApiClient;
import com.example.movies.external.api.dto.OmdbDetailResponse;
import com.example.movies.external.api.dto.OmdbMovieHit;
import com.example.movies.external.api.dto.OmdbSearchResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OmdbProviderTest {
  private final OmdbApiClient client = mock(OmdbApiClient.class);
  private final MovieProperties props = mock(MovieProperties.class);
  private final OmdbProvider provider = new OmdbProvider(client, props);

  @BeforeEach
  void setUp() {
    when(props.detailConcurrency()).thenReturn(1);
  }

  @Test
  void enrichesSearchHit() {
    OmdbSearchResponse response = read("omdb/search-1.json", OmdbSearchResponse.class);
    OmdbMovieHit hit = response
        .search()
        .getFirst();
    OmdbDetailResponse detail = read("omdb/detail-" + hit.imdbId() + ".json", OmdbDetailResponse.class);
    when(props.maxResults()).thenReturn(1);
    when(client.search("Batman", 1)).thenReturn(Mono.just(response));
    when(client.findByImdbId(hit.imdbId())).thenReturn(Mono.just(detail));

    StepVerifier
        .create(provider.search("Batman"))
        .expectNextMatches(movie -> movie
            .title()
            .equals(detail.title()) && movie
            .year()
            .equals(detail.year()) && movie
            .directors()
            .equals(com.example.movies.util.MovieUtil.splitDirectors(detail.director())))
        .verifyComplete();
    verify(client).findByImdbId(hit.imdbId());
  }

  @Test
  void returnsEmptyWhenMovieNotFound() {
    when(client.search("Missing", 1)).thenReturn(Mono.just(read("omdb/not-found.json", OmdbSearchResponse.class)));
    StepVerifier
        .create(provider.search("Missing"))
        .verifyComplete();
  }

  @Test
  void propagatesProviderError() {
    when(client.search("Batman", 1)).thenReturn(Mono.just(new OmdbSearchResponse("False", "Invalid API key!", null, null)));
    StepVerifier
        .create(provider.search("Batman"))
        .expectError(ProviderException.class)
        .verify();
  }

  @Test
  void loadsNextPageWhenLimitAllows() {
    when(props.detailConcurrency()).thenReturn(1);
    when(client.search("Batman", 1)).thenReturn(
        Mono.just(new OmdbSearchResponse("True", null, List.of(new OmdbMovieHit("tt1", "Batman", "1989")), "11")));
    when(client.search("Batman", 2)).thenReturn(
        Mono.just(new OmdbSearchResponse("True", null, List.of(new OmdbMovieHit("tt2", "Batman Returns", "1992")), "11")));
    when(client.findByImdbId("tt1")).thenReturn(Mono.just(new OmdbDetailResponse("Batman", "1989", "Tim Burton", "True", null)));
    when(client.findByImdbId("tt2")).thenReturn(Mono.just(new OmdbDetailResponse("Batman Returns", "1992", "Tim Burton", "True", null)));
    StepVerifier
        .create(provider.search("Batman"))
        .expectNext(new Movie("Batman", "1989", List.of("Tim Burton")), new Movie("Batman Returns", "1992", List.of("Tim Burton")))
        .verifyComplete();
    verify(client).search("Batman", 2);
  }

  @Test
  void stopsPaginationAtLimit() {
    when(props.maxResults()).thenReturn(1);
    when(props.detailConcurrency()).thenReturn(1);
    when(client.search("Batman", 1)).thenReturn(
        Mono.just(new OmdbSearchResponse("True", null, List.of(new OmdbMovieHit(null, "Batman", "1989")), "11")));
    when(client.findByTitle("Batman")).thenReturn(Mono.just(new OmdbDetailResponse(null, null, "Tim Burton", "True", null)));
    StepVerifier
        .create(provider.search("Batman"))
        .expectNext(new Movie("Batman", "1989", List.of("Tim Burton")))
        .verifyComplete();
    verify(client, never()).search("Batman", 2);
  }
}
