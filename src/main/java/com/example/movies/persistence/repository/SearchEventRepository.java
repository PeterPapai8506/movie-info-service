package com.example.movies.persistence.repository;

import com.example.movies.persistence.entity.SearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchEventRepository extends JpaRepository<SearchEvent, Long> {
}
