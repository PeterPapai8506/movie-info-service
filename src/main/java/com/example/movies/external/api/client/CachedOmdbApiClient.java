package com.example.movies.external.api.client;

import com.example.movies.cache.ReactiveRedisCache;
import com.example.movies.config.MovieProperties;
import com.example.movies.external.api.dto.OmdbDetailResponse;
import com.example.movies.external.api.dto.OmdbSearchResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CachedOmdbApiClient implements OmdbApiClient {

  private static final String PREFIX = "movies:v1:omdb:";

  private final OmdbApiClient delegate;
  private final ReactiveRedisCache cache;
  private final MovieProperties props;

  @Override
  public Mono<OmdbSearchResponse> search(String title, int page) {
    String key = PREFIX + "search:" + page + ":" + title;

    return cache.getOrLoad(key, OmdbSearchResponse.class, props
        .cache()
        .ttl(), () -> delegate.search(title, page), response -> !ERROR.equalsIgnoreCase(response.response()));
  }

  @Override
  public Mono<OmdbDetailResponse> findByImdbId(String imdbId) {
    String key = PREFIX + "detail:id:" + imdbId;

    return cache.getOrLoad(key, OmdbDetailResponse.class, props
        .cache()
        .ttl(), () -> delegate.findByImdbId(imdbId), response -> !ERROR.equalsIgnoreCase(response.response()));
  }

  @Override
  public Mono<OmdbDetailResponse> findByTitle(String title) {
    String key = PREFIX + "detail:title:" + title;

    return cache.getOrLoad(key, OmdbDetailResponse.class, props
        .cache()
        .ttl(), () -> delegate.findByTitle(title), response -> !ERROR.equalsIgnoreCase(response.response()));
  }
}