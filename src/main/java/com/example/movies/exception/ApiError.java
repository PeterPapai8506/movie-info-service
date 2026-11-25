package com.example.movies.exception;

public record ApiError(int status, String error) {
}
