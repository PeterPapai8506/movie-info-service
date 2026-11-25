package com.example.movies.external.api.client;

import static com.example.movies.util.ApiFixtures.read;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.example.movies.cache.ReactiveRedisCache;
import com.example.movies.config.MovieProperties;
import com.example.movies.external.api.dto.OmdbDetailResponse;
import com.example.movies.external.api.dto.OmdbSearchResponse;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CachedOmdbApiClientTest {
  private static final Duration TTL = Duration.ofHours(24);
  private final OmdbApiClient delegate = mock(OmdbApiClient.class);
  private final ReactiveRedisCache cache = mock(ReactiveRedisCache.class);
  private final MovieProperties props = mock(MovieProperties.class);
  private final CachedOmdbApiClient client = new CachedOmdbApiClient(delegate, cache, props);

  @Test
  void cachesSearchWithExpectedKey() {
    OmdbSearchResponse response = read("omdb/search-2.json", OmdbSearchResponse.class);
    when(props.cache()).thenReturn(new MovieProperties.Cache(TTL, Duration.ofMillis(500)));
    when(delegate.search("Batman", 2)).thenReturn(Mono.just(response));
    when(cache.getOrLoad(eq("movies:v1:omdb:search:2:Batman"), eq(OmdbSearchResponse.class), eq(TTL), any(), any())).thenAnswer(
        invocation -> invocation
            .<Supplier<Mono<OmdbSearchResponse>>>getArgument(3)
            .get());
    StepVerifier
        .create(client.search("Batman", 2))
        .expectNext(response)
        .verifyComplete();
  }

  @Test
  void cachesFindByImdbIdWithExpectedKey() {
    OmdbDetailResponse response = read("omdb/detail-tt0096895.json", OmdbDetailResponse.class);
    when(props.cache()).thenReturn(new MovieProperties.Cache(TTL, Duration.ofMillis(500)));
    when(delegate.findByImdbId("tt0096895")).thenReturn(Mono.just(response));
    when(cache.getOrLoad(eq("movies:v1:omdb:detail:id:tt0096895"), eq(OmdbDetailResponse.class), eq(TTL), any(), any())).thenAnswer(
        invocation -> invocation
            .<Supplier<Mono<OmdbDetailResponse>>>getArgument(3)
            .get());
    StepVerifier
        .create(client.findByImdbId("tt0096895"))
        .expectNext(response)
        .verifyComplete();
  }

  @Test
  void cachesFindByTitleWithExpectedKey() {
    OmdbDetailResponse response = read("omdb/detail-title-batman.json", OmdbDetailResponse.class);
    when(props.cache()).thenReturn(new MovieProperties.Cache(TTL, Duration.ofMillis(500)));
    when(delegate.findByTitle("Batman")).thenReturn(Mono.just(response));
    when(cache.getOrLoad(eq("movies:v1:omdb:detail:title:Batman"), eq(OmdbDetailResponse.class), eq(TTL), any(), any())).thenAnswer(
        invocation -> invocation
            .<Supplier<Mono<OmdbDetailResponse>>>getArgument(3)
            .get());
    StepVerifier
        .create(client.findByTitle("Batman"))
        .expectNext(response)
        .verifyComplete();
  }
}
