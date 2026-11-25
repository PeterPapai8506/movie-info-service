package com.example.movies.service;

import com.example.movies.config.MovieProperties;
import com.example.movies.dto.ApiName;
import com.example.movies.dto.Movie;
import com.example.movies.exception.UnsupportedApiException;
import com.example.movies.provider.MovieProvider;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

  private final Map<ApiName, MovieProvider> providers;
  private final SearchRecorder recorder;
  private final MovieProperties props;

  public Flux<Movie> find(String title, ApiName api) {

    String normalizedTitle = title.trim();

    return Flux.defer(() -> {
      MovieProvider provider = providers.get(api);

      if (Objects.isNull(provider)) {
        return Flux.error(new UnsupportedApiException(api.name()));
      }

      try {
        recorder.record(normalizedTitle, api);
      } catch (TaskRejectedException exception) {
        log.warn("Search recorder executor is overloaded", exception);
      }

      Flux<Movie> movies = provider.search(normalizedTitle);

      return props.maxResults() > 0 ? movies.take(props.maxResults()) : movies;
    });
  }
}