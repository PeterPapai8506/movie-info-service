package com.example.movies.util;

import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import tools.jackson.databind.json.JsonMapper;

public final class ApiFixtures {
  private static final JsonMapper MAPPER = JsonMapper
      .builder()
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  private ApiFixtures() {
  }

  public static <T> T read(String path, Class<T> type) {
    try (InputStream input = Objects.requireNonNull(ApiFixtures.class.getResourceAsStream("/fixtures/" + path), "Missing fixture: " + path)) {
      return MAPPER.readValue(input, type);
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot read fixture: " + path, exception);
    }
  }
}
