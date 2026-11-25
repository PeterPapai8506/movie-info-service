package com.example.movies.provider;

import static com.example.movies.external.api.client.OmdbApiClient.ERROR;
import static com.example.movies.external.api.client.OmdbApiClient.NOT_FOUND;
import static com.example.movies.util.MovieUtil.releaseYear;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import com.example.movies.config.MovieProperties;
import com.example.movies.dto.ApiName;
import com.example.movies.dto.Movie;
import com.example.movies.external.api.client.OmdbApiClient;
import com.example.movies.external.api.dto.OmdbDetailResponse;
import com.example.movies.external.api.dto.OmdbMovieHit;
import com.example.movies.external.api.dto.OmdbSearchResponse;
import com.example.movies.util.MovieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OmdbProvider extends AbstractMovieProvider {
  private static final int PAGE_SIZE = 10;

  private final OmdbApiClient client;
  private final MovieProperties props;

  @Override
  public ApiName api() {
    return ApiName.OMDB;
  }

  @Override
  public Flux<Movie> search(String title) {
    return client
        .search(title, 1)
        .flatMapMany(first -> searchPages(title, first))
        .flatMap(this::enrichMovie, props.detailConcurrency());
  }

  private Flux<OmdbMovieHit> searchPages(String title, OmdbSearchResponse first) {
    if (ERROR.equalsIgnoreCase(first.response())) {
      return NOT_FOUND.equalsIgnoreCase(first.error()) ? Flux.empty() : Flux.error(providerError(first.error()));
    }

    int totalPages = Math.max(1, Math.ceilDiv(toInt(first.totalResults()), PAGE_SIZE));

    return paginate(first.search(), totalPages, props.maxResults(), page -> client
        .search(title, page)
        .flatMapMany(this::pageHits));
  }

  private Flux<OmdbMovieHit> pageHits(OmdbSearchResponse response) {
    return ERROR.equalsIgnoreCase(response.response()) ? Flux.error(providerError(response.error())) : hits(response.search());
  }

  private Mono<Movie> enrichMovie(OmdbMovieHit hit) {
    Mono<OmdbDetailResponse> request = isNotBlank(hit.imdbId()) ? client.findByImdbId(hit.imdbId()) : client.findByTitle(hit.title());

    return request.flatMap(
        response -> ERROR.equalsIgnoreCase(response.response()) ? Mono.error(providerError(response.error())) : Mono.just(toMovie(response, hit)));
  }

  private Movie toMovie(OmdbDetailResponse detail, OmdbMovieHit hit) {
    return new Movie(defaultIfBlank(detail.title(), hit.title()), releaseYear(defaultIfBlank(detail.year(), hit.year())),
        MovieUtil.splitDirectors(detail.director()));
  }
}