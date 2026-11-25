package com.example.movies.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;

class MovieUtilTest {
  @Test
  void splitsAndTrimsDirectors() {
    assertEquals(List.of("Alice", "Bob"), MovieUtil.splitDirectors(" Alice, , Bob "));
  }

  @Test
  void handlesMissingDirectors() {
    assertEquals(List.of(), MovieUtil.splitDirectors(null));
    assertEquals(List.of(), MovieUtil.splitDirectors(" "));
    assertEquals(List.of(), MovieUtil.splitDirectors("n/a"));
  }
}
