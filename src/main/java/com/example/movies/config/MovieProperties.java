package com.example.movies.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "movie")
public record MovieProperties(@Valid @NotNull Omdb omdb, @Valid @NotNull Tmdb tmdb, @Min(1) int detailConcurrency, @Min(0) int maxResults,
                              @Valid @NotNull Cache cache, @Min(1) int connectTimeoutMillis, @NotNull Duration responseTimeout) {

  public record Omdb(@NotBlank String baseUrl, @NotBlank String apiKey, @NotBlank String apiKeyParam) {
  }

  public record Tmdb(@NotBlank String baseUrl, @NotBlank String apiKey, @NotBlank String apiKeyParam) {
  }

  public record Cache(@NotNull Duration ttl, @NotNull Duration operationTimeout) {
  }
}