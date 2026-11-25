package com.example.movies.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.example.movies.exception.UnsupportedApiException;
import org.junit.jupiter.api.Test;

class ApiNameTest {
  @Test
  void parsesSupportedValues() {
    assertEquals(ApiName.OMDB, ApiName.parse(" omdb "));
    assertEquals(ApiName.TMDB, ApiName.parse("TmDb"));
  }

  @Test
  void rejectsMissingValues() {
    assertThrows(UnsupportedApiException.class, () -> ApiName.parse(null));
    assertThrows(UnsupportedApiException.class, () -> ApiName.parse(" "));
  }

  @Test
  void preservesInvalidValueCause() {
    UnsupportedApiException error = assertThrows(UnsupportedApiException.class, () -> ApiName.parse("invalid"));
    assertEquals(400, error
        .getStatusCode()
        .value());
    assertNotNull(error.getCause());
  }
}
