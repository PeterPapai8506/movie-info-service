package com.example.movies.api;

import com.example.movies.dto.MovieResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

@Tag(name = "Movies", description = "Movie information API")
@Validated
@RequestMapping("/movies")
public interface MovieApi {
  @Operation(summary = "Search movies", description = "Search movies using OMDb or TMDb")
  @ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
      , schema = @Schema(implementation = MovieResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid request parameters")
  @ApiResponse(responseCode = "502", description = "External movie API error")
  @GetMapping(value = "/{movieTitle}", produces = MediaType.APPLICATION_JSON_VALUE)
  Flux<DataBuffer> movies(
      @Parameter(description = "Movie title", example = "Avengers", required = true) @PathVariable("movieTitle")
      @NotBlank @Size(max = 255) String movieTitle,
      @Parameter(description = "Movie data provider", required = true, schema = @Schema(defaultValue = "OMDB", allowableValues = {"OMDB",
          "TMDB"})) @RequestParam("api") @NotBlank String api);
}
