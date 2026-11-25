package com.example.movies.provider;

import static com.example.movies.util.MovieUtil.releaseYear;
import com.example.movies.config.MovieProperties;
import com.example.movies.dto.ApiName;
import com.example.movies.dto.Movie;
import com.example.movies.external.api.client.TmdbApiClient;
import com.example.movies.external.api.dto.TmdbCredits;
import com.example.movies.external.api.dto.TmdbCredits.CrewMember;
import com.example.movies.external.api.dto.TmdbMovieHit;
import com.example.movies.external.api.dto.TmdbSearchResponse;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TmdbProvider extends AbstractMovieProvider {

  private static final String DIRECTOR = "Director";

  private final TmdbApiClient client;
  private final MovieProperties props;

  @Override
  public ApiName api() {
    return ApiName.TMDB;
  }

  @Override
  public Flux<Movie> search(String title) {
    return client
        .search(title, 1)
        .flatMapMany(first -> searchPages(title, first))
        .flatMap(this::enrichMovie, props.detailConcurrency());
  }

  private Flux<TmdbMovieHit> searchPages(String title, TmdbSearchResponse first) {
    return paginate(first.results(), first.totalPages(), props.maxResults(), page -> client
        .search(title, page)
        .flatMapMany(response -> hits(response.results())));
  }

  private Mono<Movie> enrichMovie(TmdbMovieHit hit) {
    return client
        .credits(hit.id())
        .map(credits -> toMovie(credits, hit));
  }

  private Movie toMovie(TmdbCredits credits, TmdbMovieHit hit) {
    return new Movie(hit.title(), releaseYear(hit.releaseDate()), directors(credits));
  }

  private List<String> directors(TmdbCredits credits) {
    return emptyIfNull(credits.crew())
        .stream()
        .filter(member -> DIRECTOR.equalsIgnoreCase(member.job()))
        .map(CrewMember::name)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }
}