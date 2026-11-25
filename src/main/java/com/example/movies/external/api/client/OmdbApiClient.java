package com.example.movies.external.api.client;

import com.example.movies.external.api.dto.OmdbDetailResponse;
import com.example.movies.external.api.dto.OmdbSearchResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface OmdbApiClient {
  String NOT_FOUND = "Movie not found!";
  String ERROR = "False";

  @GetExchange
  Mono<OmdbSearchResponse> search(@RequestParam("s") String title, @RequestParam("page") int page);

  @GetExchange
  Mono<OmdbDetailResponse> findByImdbId(@RequestParam("i") String imdbId);

  @GetExchange
  Mono<OmdbDetailResponse> findByTitle(@RequestParam("t") String title);
}
