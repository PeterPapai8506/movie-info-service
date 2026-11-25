package com.example.movies.util;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.util.TokenBuffer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveJsonArrayTemplateEncoder {
  private static final DataBufferFactory BUFFER_FACTORY = DefaultDataBufferFactory.sharedInstance;
  private final ObjectMapper objectMapper;

  public <T> Flux<DataBuffer> encode(Object template, Flux<T> items) {
    return items.switchOnFirst((signal, flux) -> {
      if (signal.isOnError()) {
        return Flux.error(signal.getThrowable());
      }

      return Flux.using(() -> new EncodingContext(template), context -> Flux.concat(Mono.fromSupplier(context::start), flux
          .onErrorResume(error -> {
            log.error("JSON stream interrupted", error);
            return Flux.empty();
          })
          .map(context::write), Mono.fromSupplier(context::end)), EncodingContext::close);
    });
  }

  private final class EncodingContext implements AutoCloseable {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final TokenBuffer tokens;
    private final JsonParser parser;
    private final JsonGenerator generator;

    private EncodingContext(Object template) {
      tokens = objectMapper.writeValueIntoBuffer(template);
      parser = tokens.asParser();
      generator = objectMapper.createGenerator(output, JsonEncoding.UTF8);
    }

    private DataBuffer start() {
      JsonToken token = parser.nextToken();

      while (Objects.nonNull(token)) {
        generator.copyCurrentEvent(parser);

        if (JsonToken.START_ARRAY.equals(token)) {
          token = parser.nextToken();

          if (JsonToken.END_ARRAY.equals(token)) {
            return toBuffer();
          }

          if (token != null) {
            continue;
          }

          break;
        }

        token = parser.nextToken();
      }

      throw new IllegalArgumentException("Template must contain an empty array");
    }

    private DataBuffer write(Object item) {
      objectMapper.writeValue(generator, item);

      return toBuffer();
    }

    private DataBuffer end() {
      do {
        generator.copyCurrentEvent(parser);
      } while (parser.nextToken() != null);

      return toBuffer();
    }

    private DataBuffer toBuffer() {
      generator.flush();

      byte[] bytes = output.toByteArray();
      output.reset();

      return BUFFER_FACTORY.wrap(bytes);
    }

    @Override
    public void close() {
      try (tokens; parser; generator) {
        // Resources are closed automatically.
      }
    }
  }
}
