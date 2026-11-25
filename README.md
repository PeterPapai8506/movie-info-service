# Movie Info Service

A reactive movie information service integrating OMDb and TMDb.

## Technology Stack

- Java 25
- Spring Boot 4.2
- Spring WebFlux
- Project Reactor
- Spring HTTP Interface Clients
- Redis
- Resilience4j
- Spring Data JPA
- MySQL
- Flyway
- Lombok
- Maven
- Docker Compose
- OpenAPI and Swagger UI
- Checkstyle and PMD

## Configuration

Use `.env` file to configure the required API keys.

Configure the environment variables in the IDE run configuration when running the application locally.

## Running with Docker

From the project root:

```bash
cd docker
docker compose up -d --build
```

The Docker Compose configuration starts the application, MySQL and Redis.

## Running Locally

Start the required infrastructure:

```bash
cd docker
docker compose up -d mysql redis
cd ..
```

Load the environment variables from the root `.env` file and start the application:

```bash
mvn spring-boot:run
```

## API Usage

```bash
curl -N "http://localhost:8080/movies/Avengers?api=OMDB"
curl -N "http://localhost:8080/movies/Avengers?api=TMDB"
```

Supported providers: `OMDB` and `TMDB`.

Example response:

```json
{
  "movies": [
    {
      "Title": "The Avengers",
      "Year": "2012",
      "Director": ["Joss Whedon"]
    }
  ]
}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Architecture

The application uses reactive HTTP clients and provider-specific pagination.

Movie details and credits are retrieved concurrently with configurable concurrency limits. Result ordering is not guaranteed.

The maximum number of results is configurable. A value of zero disables the application-level result limit.

External API responses are cached individually in Redis using the cache-aside pattern.

The default cache TTL is 24 hours. Redis operations are protected by a Resilience4j circuit breaker and configurable operation timeouts.

Redis failures do not prevent external API requests.

Search events are persisted asynchronously in MySQL using a dedicated executor. Search event recording is best-effort.

Flyway manages database schema migrations.

## Streaming and Error Handling

Movie results are streamed as a JSON document without collecting the complete result set in memory.

Provider failures occurring before streaming begins are returned as HTTP errors.

If the upstream result stream fails after streaming has started, the application logs the failure and attempts to complete the JSON document with the results already retrieved.

Serialization failures and client disconnections may prevent successful completion of the response.

## Build and Code Quality

```bash
mvn clean verify
```

Checkstyle and PMD are integrated into the Maven build.
