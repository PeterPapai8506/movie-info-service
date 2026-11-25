package com.example.movies.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI movieOpenApi() {
    return new OpenAPI().info(new Info()
        .title("Movie Info Service API")
        .version("1.0.0")
        .description("Reactive movie search API using OMDb and TMDb"));
  }
}
