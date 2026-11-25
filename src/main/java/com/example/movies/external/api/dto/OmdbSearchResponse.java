package com.example.movies.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OmdbSearchResponse(@JsonProperty("Response") String response, @JsonProperty("Error") String error,
                                 @JsonProperty("Search") List<OmdbMovieHit> search, @JsonProperty("totalResults") String totalResults) {
}
