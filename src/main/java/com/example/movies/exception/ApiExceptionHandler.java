package com.example.movies.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleValidation(ConstraintViolationException exception) {
    return buildResponse(BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException exception) {
    return buildResponse(exception.getStatusCode(), exception.getReason(), exception.getHeaders());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
    log.error("Unexpected application error", exception);

    return createResponse(INTERNAL_SERVER_ERROR, "Internal server error", HttpHeaders.EMPTY);
  }

  private ResponseEntity<ApiError> buildResponse(HttpStatusCode status, String message) {
    return buildResponse(status, message, HttpHeaders.EMPTY);
  }

  private ResponseEntity<ApiError> buildResponse(HttpStatusCode status, String message, HttpHeaders headers) {
    log.warn("Request failed: HTTP {} - {}", status.value(), message);

    return createResponse(status, message, headers);
  }

  private ResponseEntity<ApiError> createResponse(HttpStatusCode status, String message, HttpHeaders headers) {
    return ResponseEntity
        .status(status)
        .headers(headers)
        .contentType(APPLICATION_JSON)
        .body(new ApiError(status.value(), message));
  }
}