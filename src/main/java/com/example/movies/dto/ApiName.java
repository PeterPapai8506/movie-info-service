package com.example.movies.dto;

import com.example.movies.exception.UnsupportedApiException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum ApiName {
  OMDB, TMDB;

  public static ApiName parse(String value) {
    if (value == null || value.isBlank()) {
      throw new UnsupportedApiException("API is required. Supported values: " + supportedValues());
    }

    try {
      return valueOf(value
          .trim()
          .toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new UnsupportedApiException("Unsupported API: " + value + ". Supported values: " + supportedValues(), exception);
    }
  }

  private static String supportedValues() {
    return Arrays
        .stream(values())
        .map(api -> api
            .name()
            .toLowerCase(Locale.ROOT))
        .collect(Collectors.joining(", "));
  }
}