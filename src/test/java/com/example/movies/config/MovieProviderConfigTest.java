package com.example.movies.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.example.movies.dto.ApiName;
import com.example.movies.provider.MovieProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MovieProviderConfigTest {
  @Test
  void indexesProvidersByApi() {
    MovieProvider omdb = mock(MovieProvider.class);
    MovieProvider tmdb = mock(MovieProvider.class);
    when(omdb.api()).thenReturn(ApiName.OMDB);
    when(tmdb.api()).thenReturn(ApiName.TMDB);
    Map<ApiName, MovieProvider> providers = new MovieProviderConfig().movieProviders(List.of(omdb, tmdb));
    assertEquals(2, providers.size());
    assertSame(omdb, providers.get(ApiName.OMDB));
    assertSame(tmdb, providers.get(ApiName.TMDB));
  }

  @Test
  void handlesEmptyProviderList() {
    assertEquals(Map.of(), new MovieProviderConfig().movieProviders(List.of()));
  }
}
