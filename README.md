# Orbit Telemetry

A Java service prototype exploring HTTP health reporting, Prometheus-format metrics, and resilience around a simulated external dependency.

> **Project status:** Learning prototype. The dependency is simulated; this is not a live telemetry collector or a production reliability service.

## Implemented endpoints

| Endpoint | Behavior |
|---|---|
| `GET /health` | Returns a JSON `UP` status |
| `GET /metrics` | Exposes the Micrometer Prometheus registry scrape |
| `GET /api/data` | Runs a simulated dependency call through a Resilience4j circuit breaker |

The simulated call returns demonstration telemetry for a deterministic test sample and throws for a failure sample. The HTTP route uses a random sample, so its response varies.

## Resilience configuration

The circuit breaker uses:

- a 50% failure-rate threshold;
- a four-call count-based sliding window;
- a minimum of four calls before calculating the failure rate; and
- a ten-second open-state wait duration.

The minimum-call setting is explicit so the four-call example window can actually evaluate its failure rate.

## Technology

| Area | Technology |
|---|---|
| Language/runtime | Java 21 |
| HTTP | Javalin |
| Resilience | Resilience4j CircuitBreaker |
| Metrics | Micrometer and Prometheus registry |
| Logging | SLF4J |
| Build | Gradle 8.10.2 in CI |
| Tests | JUnit 5 and JaCoCo |
| Container definition | Dockerfile (not currently built by CI) |

## Repository layout

The application and test use Gradle's standard source layout:

```text
src/main/java/com/orbit/telemetry/OrbitApp.java
src/test/java/com/orbit/telemetry/OrbitAppTest.java
build.gradle
settings.gradle
Dockerfile
.github/workflows/ci.yml
```

A Gradle Wrapper is not checked in. Install a compatible Gradle version locally, then run:

```bash
gradle clean test jacocoTestReport shadowJar
```

The build produces `build/libs/orbit-telemetry.jar`.

## Tests and continuous integration

The unit tests verify the deterministic simulated success/failure paths and that four consecutive failures open the circuit breaker and reject a subsequent call.

GitHub Actions uses Java 21 and Gradle 8.10.2 to run the tests, generate a JaCoCo report, build the Shadow JAR, and upload the JAR as a workflow artifact. The workflow does not currently build or smoke-test the Docker image, nor does it test the HTTP endpoints with an embedded server.

Check the latest [Actions runs](https://github.com/Ashwani899/Java-Application-using-Gradle-/actions) for current results.

## Limitations and next steps

- Replace the random simulated dependency with an injectable HTTP client and deterministic stub server.
- Add endpoint-level tests for health, metrics, API response codes, and circuit-breaker transitions.
- Add Docker build and runtime health checks to CI.
- Add a Gradle Wrapper after generating and verifying it with the selected Gradle distribution.
- Add explicit metrics for request counts and circuit-breaker state.

The service does not connect to a live telemetry source and does not claim production reliability.

## License

See the repository license file.
