package com.example.movies.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OmdbDetailResponse(@JsonProperty("Title") String title, @JsonProperty("Year") String year, @JsonProperty("Director") String director,
                                 @JsonProperty("Response") String response, @JsonProperty("Error") String error) {
}
