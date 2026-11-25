package com.example.movies.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import java.io.Serial;
import org.springframework.web.server.ResponseStatusException;

public class UnsupportedApiException extends ResponseStatusException {
  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedApiException(String message) {
    super(BAD_REQUEST, message);
  }

  public UnsupportedApiException(String message, Throwable cause) {
    super(BAD_REQUEST, message, cause);
  }
}