package com.example.movies.provider;

import com.example.movies.dto.ApiName;
import com.example.movies.dto.Movie;
import reactor.core.publisher.Flux;

public interface MovieProvider {
  ApiName api();

  Flux<Movie> search(String title);

}
