package com.example.movies.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbSearchResponse(@JsonProperty("total_pages") int totalPages, List<TmdbMovieHit> results) {
}
