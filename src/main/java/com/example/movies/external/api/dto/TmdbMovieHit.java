package com.example.movies.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieHit(long id, String title, @JsonProperty("release_date") String releaseDate) {
}

