package com.example.movies.exception;

import java.io.Serial;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class ProviderException extends ResponseStatusException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ProviderException(HttpStatusCode status, String message) {
    super(status, message);
  }

  public ProviderException(HttpStatusCode status, String message, Throwable cause) {
    super(status, message, cause);
  }
}