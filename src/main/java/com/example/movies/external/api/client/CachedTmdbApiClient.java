package com.example.movies.external.api.client;

import com.example.movies.cache.ReactiveRedisCache;
import com.example.movies.config.MovieProperties;
import com.example.movies.external.api.dto.TmdbCredits;
import com.example.movies.external.api.dto.TmdbSearchResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CachedTmdbApiClient implements TmdbApiClient {

  private static final String PREFIX = "movies:v1:tmdb:";

  private final TmdbApiClient delegate;
  private final ReactiveRedisCache cache;
  private final MovieProperties props;

  @Override
  public Mono<TmdbSearchResponse> search(String title, int page) {
    String key = PREFIX + "search:" + page + ":" + title;

    return cache.getOrLoad(key, TmdbSearchResponse.class, props
        .cache()
        .ttl(), () -> delegate.search(title, page));
  }

  @Override
  public Mono<TmdbCredits> credits(long id) {
    String key = PREFIX + "credits:" + id;

    return cache.getOrLoad(key, TmdbCredits.class, props
        .cache()
        .ttl(), () -> delegate.credits(id));
  }
}