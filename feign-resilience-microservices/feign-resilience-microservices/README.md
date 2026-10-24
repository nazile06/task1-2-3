# Feign Resilience Microservices

Two Spring Boot microservices that communicate through OpenFeign, run together in Docker Compose, and use PostgreSQL for storage. Task 3 adds **retry**, **error propagation** and a **circuit breaker** (Resilience4j).

| Service | Port | Role |
|---|---|---|
| `client-service` | 8081 | Public API. Processes requests and calls `storage-service` through Feign. |
| `storage-service` | 8082 | Internal service. Stores data in PostgreSQL. |
| `postgres` | 5432 | PostgreSQL 16 (`storagedb`). |

```
Postman -> client-service (8081) --Feign--> storage-service (8082) --> PostgreSQL
           retry + circuit breaker + error mapping
```

## Run

Requirements: Docker Desktop running.

```bash
docker compose up --build
```

Stop with `docker compose down` (add `-v` to delete the database volume).

Local run without Docker: start PostgreSQL (`storagedb`, user/password `storage`), then `mvn spring-boot:run` in `storage-service` and `client-service` (Java 17+, Maven 3.9+).

## Endpoints (client-service, port 8081)

| Method | Path | Description |
|---|---|---|
| GET | `/api/hello` | Part 1: Feign call to storage-service |
| POST | `/api/items` | Part 2: processes the item (trim, uppercase name), forwards it, stores it in PostgreSQL |
| GET | `/api/items` | List stored items |
| GET | `/api/items/{id}` | Get one item (404 if missing) |
| GET | `/api/test/error` | Always fails with 500 in storage-service |
| GET | `/api/test/flaky?failures=N` | Fails N times, then succeeds |
| GET | `/api/test/slow?ms=5000` | Slow response, triggers the Feign read timeout |
| POST | `/api/test/reset` | Resets the flaky counter |
| GET | `/api/circuit-breaker/state` | Current circuit breaker state and metrics |

Actuator: `/actuator/health`, `/actuator/circuitbreakers`, `/actuator/circuitbreakerevents`, `/actuator/retries`, `/actuator/retryevents`.

## Task 3 design

All calls to storage-service go through `StorageGateway`, annotated with `@Retry(name = "storage")` and `@CircuitBreaker(name = "storage")`. Retry is the outer layer, the circuit breaker the inner layer. Configuration is in `client-service/src/main/resources/application.yml`.

### Retry
- Up to 3 attempts, 500 ms initial wait, exponential backoff (x2).
- Retried: 5xx responses (`DownstreamServerException`) and connection or timeout failures (`feign.RetryableException`).
- Not retried: 4xx responses (`DownstreamClientException`) and an open circuit.
- Feign's own retryer is disabled, so Resilience4j is the only retry mechanism.
- Note: retrying a non-idempotent `POST` after a timeout can create duplicates. Acceptable for this exercise; production systems should use idempotency keys.

### Error propagation
- `storage-service` returns a uniform JSON error body: `timestamp, status, error, message, path, source`.
- A Feign `ErrorDecoder` (`FeignConfig`) converts error responses into `DownstreamClientException` (4xx) or `DownstreamServerException` (5xx), keeping the original status and message.
- `client-service` `GlobalExceptionHandler` returns the same status and message to the caller with `source: storage-service`.
- Storage unreachable or timed out after retries: `503`. Circuit open: `503` ("Circuit breaker is OPEN"). Invalid input: `400`.

### Circuit breaker
- Count-based window of 10 calls, minimum 5 calls, opens at 50% failure rate.
- Stays OPEN for 15 s, then HALF_OPEN with 3 trial calls. Success closes it, failure reopens it.
- 4xx responses are ignored (neither success nor failure).

## Demo scenarios (Postman)

Import `postman/feign-resilience-microservices.postman_collection.json` and run the folders in order. Watch the `client-service` logs (`docker compose logs -f client-service`) to see one "Calling storage-service" line per attempt.

1. **Retry**: `GET /api/test/flaky?failures=2` returns 200 after 3 attempts. With `failures=5` all attempts fail and 500 is propagated.
2. **Error propagation**: `GET /api/items/999999` returns 404 with the storage-service message. A blank name returns 400.
3. **Circuit breaker**:
   1. `GET /api/circuit-breaker/state` shows `CLOSED`.
   2. Call `GET /api/test/error` three or more times. State becomes `OPEN`.
   3. `GET /api/hello` now returns 503 immediately without calling storage-service.
   4. Wait 15 s, call `GET /api/hello` three times. State goes `HALF_OPEN`, then `CLOSED`.
4. **Service down**: `docker compose stop storage-service`, call `GET /api/hello`. After retries you get 503 and the breaker eventually opens. Restart with `docker compose start storage-service`.

## Submission checklist

```bash
git init
git add .
git commit -m "Add Feign services, PostgreSQL, retry, error propagation and circuit breaker"
git branch -M main
git remote add origin <your-private-repo-url>
git push -u origin main
```

- Repository is **private**.
- Add `sadiq.rzazade@khazar.org` as a collaborator.
- Postman collection is committed under `postman/`.
