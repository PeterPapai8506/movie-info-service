package com.example.movies.service;

import com.example.movies.dto.ApiName;
import com.example.movies.persistence.entity.SearchEvent;
import com.example.movies.persistence.repository.SearchEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchRecorder {

  private final SearchEventRepository repository;

  @Async("searchRecorderExecutor")
  public void record(String title, ApiName api) {

    try {
      repository.save(new SearchEvent(title, api.name()));
    } catch (RuntimeException exception) {
      log.warn("Could not persist search event", exception);
    }
  }
}
