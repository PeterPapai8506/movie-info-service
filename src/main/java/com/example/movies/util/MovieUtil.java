package com.example.movies.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.left;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MovieUtil {

  public static List<String> splitDirectors(String directors) {
    if (isBlank(directors) || "N/A".equalsIgnoreCase(directors)) {
      return List.of();
    }

    return Arrays
        .stream(directors.split(","))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .distinct()
        .toList();
  }

  public static String releaseYear(String value) {
    return Objects.isNull(value) || value.length() < 4 ? "" : left(value, 4);
  }
}
