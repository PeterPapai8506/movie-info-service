package com.example.movies.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OmdbMovieHit(@JsonProperty("imdbID") String imdbId, @JsonProperty("Title") String title, @JsonProperty("Year") String year) {
}
