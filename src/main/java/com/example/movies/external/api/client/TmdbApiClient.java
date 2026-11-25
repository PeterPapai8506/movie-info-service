package com.example.movies.external.api.client;

import com.example.movies.external.api.dto.TmdbCredits;
import com.example.movies.external.api.dto.TmdbSearchResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface TmdbApiClient {

  @GetExchange("/search/movie?include_adult=true")
  Mono<TmdbSearchResponse> search(@RequestParam("query") String title, @RequestParam("page") int page);

  @GetExchange("/movie/{id}/credits")
  Mono<TmdbCredits> credits(@PathVariable("id") long id);
}
