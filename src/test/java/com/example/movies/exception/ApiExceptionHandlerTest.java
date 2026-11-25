package com.example.movies.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class ApiExceptionHandlerTest {
  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void preservesResponseStatusAndReason() {
    ResponseEntity<ApiError> response = handler.handleResponseStatus(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Provider failed"));
    assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response
        .getHeaders()
        .getContentType());
    assertEquals(new ApiError(502, "Provider failed"), response.getBody());
  }

  @Test
  void hidesUnexpectedErrorDetails() {
    ResponseEntity<ApiError> response = handler.handleUnexpected(new IllegalStateException("Private details"));
    assertEquals(new ApiError(500, "Internal server error"), response.getBody());
  }
}
