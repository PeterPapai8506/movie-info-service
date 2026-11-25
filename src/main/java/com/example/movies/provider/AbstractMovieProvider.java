package com.example.movies.provider;

import com.example.movies.exception.ProviderException;
import java.util.List;
import java.util.function.IntFunction;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;

public abstract class AbstractMovieProvider implements MovieProvider {

  protected <T> Flux<T> paginate(List<T> firstPage, int totalPages, int maxResults, IntFunction<Flux<T>> pageLoader) {
    Flux<T> remainingPages = Flux
        .range(2, Math.max(0, totalPages - 1))
        .concatMap(pageLoader::apply, 0);

    Flux<T> results = Flux.concat(hits(firstPage), remainingPages);

    return maxResults > 0 ? results.take(maxResults) : results;
  }

  protected <T> Flux<T> hits(List<T> results) {
    return Flux.fromIterable(emptyIfNull(results));
  }

  protected <T> List<T> emptyIfNull(List<T> values) {
    return values == null ? List.of() : values;
  }

  protected ProviderException providerError(String message) {
    return new ProviderException(HttpStatus.BAD_GATEWAY, message);
  }
}