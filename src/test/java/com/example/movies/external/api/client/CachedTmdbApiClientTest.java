package com.example.movies.external.api.client;

import static com.example.movies.util.ApiFixtures.read;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.example.movies.cache.ReactiveRedisCache;
import com.example.movies.config.MovieProperties;
import com.example.movies.external.api.dto.TmdbCredits;
import com.example.movies.external.api.dto.TmdbSearchResponse;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CachedTmdbApiClientTest {
  private static final Duration TTL = Duration.ofHours(24);
  private final TmdbApiClient delegate = mock(TmdbApiClient.class);
  private final ReactiveRedisCache cache = mock(ReactiveRedisCache.class);
  private final MovieProperties props = mock(MovieProperties.class);
  private final CachedTmdbApiClient client = new CachedTmdbApiClient(delegate, cache, props);

  @Test
  void cachesSearchWithExpectedKey() {
    TmdbSearchResponse response = read("tmdb/search-2.json", TmdbSearchResponse.class);
    when(props.cache()).thenReturn(new MovieProperties.Cache(TTL, Duration.ofMillis(500)));
    when(delegate.search("Batman", 2)).thenReturn(Mono.just(response));
    when(cache.getOrLoad(eq("movies:v1:tmdb:search:2:Batman"), eq(TmdbSearchResponse.class), eq(TTL), any())).thenAnswer(invocation -> invocation
        .<Supplier<Mono<TmdbSearchResponse>>>getArgument(3)
        .get());
    StepVerifier
        .create(client.search("Batman", 2))
        .expectNext(response)
        .verifyComplete();
  }

  @Test
  void cachesCreditsWithExpectedKey() {
    TmdbCredits response = read("tmdb/credits-268.json", TmdbCredits.class);
    when(props.cache()).thenReturn(new MovieProperties.Cache(TTL, Duration.ofMillis(500)));
    when(delegate.credits(268L)).thenReturn(Mono.just(response));
    when(cache.getOrLoad(eq("movies:v1:tmdb:credits:268"), eq(TmdbCredits.class), eq(TTL), any())).thenAnswer(invocation -> invocation
        .<Supplier<Mono<TmdbCredits>>>getArgument(3)
        .get());
    StepVerifier
        .create(client.credits(268L))
        .expectNext(response)
        .verifyComplete();
  }
}
