package com.example.movies.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.movies.dto.ApiName;
import com.example.movies.persistence.entity.SearchEvent;
import com.example.movies.persistence.repository.SearchEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SearchRecorderTest {
  private final SearchEventRepository repository = mock(SearchEventRepository.class);
  private final SearchRecorder recorder = new SearchRecorder(repository);

  @Test
  void persistsSearchEvent() {
    recorder.record("Batman", ApiName.OMDB);
    ArgumentCaptor<SearchEvent> captor = ArgumentCaptor.forClass(SearchEvent.class);
    verify(repository).save(captor.capture());
    assertEquals("Batman", captor
        .getValue()
        .getMovieTitle());
    assertEquals("OMDB", captor
        .getValue()
        .getApiName());
  }

  @Test
  void toleratesRepositoryFailure() {
    when(repository.save(any(SearchEvent.class))).thenThrow(new IllegalStateException("Database unavailable"));
    assertDoesNotThrow(() -> recorder.record("Batman", ApiName.OMDB));
  }
}
