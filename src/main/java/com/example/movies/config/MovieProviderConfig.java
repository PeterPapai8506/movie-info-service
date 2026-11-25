package com.example.movies.config;

import com.example.movies.dto.ApiName;
import com.example.movies.provider.MovieProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MovieProviderConfig {

  @Bean
  public Map<ApiName, MovieProvider> movieProviders(List<MovieProvider> providers) {
    return providers
        .stream()
        .collect(Collectors.toMap(MovieProvider::api, Function.identity(), (existing, replacement) -> {
          throw new IllegalStateException("Duplicate movie provider: " + existing.api());
        }, () -> new EnumMap<>(ApiName.class)));
  }
}
