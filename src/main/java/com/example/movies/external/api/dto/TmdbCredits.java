package com.example.movies.external.api.dto;

import java.util.List;

public record TmdbCredits(List<CrewMember> crew) {

  public record CrewMember(String name, String job) {
  }
}
