package com.example.movies.controller;

import com.example.movies.api.MovieApi;
import com.example.movies.dto.ApiName;
import com.example.movies.dto.MovieResponse;
import com.example.movies.service.MovieService;
import com.example.movies.util.ReactiveJsonArrayTemplateEncoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class MovieController implements MovieApi {

  private final MovieService service;
  private final ReactiveJsonArrayTemplateEncoder encoder;

  @Override
  public Flux<DataBuffer> movies(String movieTitle, String api) {
    return Flux.defer(() -> encoder.encode(new MovieResponse(List.of()), service.find(movieTitle, ApiName.parse(api))));
  }
}
