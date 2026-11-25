package com.example.movies.config;

import com.example.movies.cache.ReactiveRedisCache;
import com.example.movies.dto.ApiName;
import com.example.movies.exception.ProviderException;
import com.example.movies.external.api.client.CachedOmdbApiClient;
import com.example.movies.external.api.client.CachedTmdbApiClient;
import com.example.movies.external.api.client.OmdbApiClient;
import com.example.movies.external.api.client.TmdbApiClient;
import io.netty.channel.ChannelOption;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

  private final MovieProperties props;
  private final WebClientLoggingFilter loggingFilter;
  private final ReactiveRedisCache cache;

  @Bean
  public OmdbApiClient omdbApiClient(WebClient.Builder builder) {
    MovieProperties.Omdb config = props.omdb();
    WebClient client = createWebClient(builder, ApiName.OMDB, config.baseUrl(), config.apiKeyParam(), config.apiKey());

    return new CachedOmdbApiClient(createClient(client, OmdbApiClient.class), cache, props);
  }

  @Bean
  public TmdbApiClient tmdbApiClient(WebClient.Builder builder) {
    MovieProperties.Tmdb config = props.tmdb();
    WebClient client = createWebClient(builder, ApiName.TMDB, config.baseUrl(), config.apiKeyParam(), config.apiKey());

    return new CachedTmdbApiClient(createClient(client, TmdbApiClient.class), cache, props);
  }

  private WebClient createWebClient(WebClient.Builder builder, ApiName api, String baseUrl, String apiKeyParam, String apiKey) {
    return builder
        .clone()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectTimeoutMillis())
            .responseTimeout(props.responseTimeout())))
        .filter(transportErrorFilter(api))
        .filter(apiKeyFilter(apiKeyParam, apiKey))
        .filter(loggingFilter.create(api.name()))
        .defaultStatusHandler(HttpStatusCode::isError, response -> response
            .createException()
            .map(error -> {
              log.warn("{} API returned HTTP {}", api, error.getStatusCode());

              return new ProviderException(HttpStatus.BAD_GATEWAY, api.name() + " API request failed", error);
            }))
        .build();
  }

  private <T> T createClient(WebClient client, Class<T> type) {
    return HttpServiceProxyFactory
        .builderFor(WebClientAdapter.create(client))
        .build()
        .createClient(type);
  }

  private ExchangeFilterFunction apiKeyFilter(String parameter, String apiKey) {
    return (request, next) -> {
      URI uri = UriComponentsBuilder
          .fromUri(request.url())
          .queryParam(parameter, apiKey)
          .build(true)
          .toUri();

      ClientRequest updated = ClientRequest
          .from(request)
          .url(uri)
          .build();

      return next.exchange(updated);
    };
  }

  private ExchangeFilterFunction transportErrorFilter(ApiName api) {
    return (request, next) -> next
        .exchange(request)
        .onErrorMap(WebClientRequestException.class,
            error -> new ProviderException(HttpStatus.BAD_GATEWAY, "%s API connection failed".formatted(api.name()), error));
  }
}