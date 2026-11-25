package com.example.movies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Movie(@JsonProperty("Title") String title, @JsonProperty("Year") String year, @JsonProperty("Director") List<String> directors) {
}
