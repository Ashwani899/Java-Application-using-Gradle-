# Orbit Telemetry

A Java service prototype exploring basic service observability and resilience patterns. The application exposes a health endpoint, a Prometheus metrics endpoint, and a sample API route protected by a Resilience4j circuit breaker around a simulated external dependency.

> **Project status:** Learning prototype. The external service is simulated, the current test is only a placeholder assertion, and the repository still needs build-layout and CI wiring before it should be described as production-ready.

## What is implemented in the current source

- **HTTP service:** Javalin application listening on port `8080`.
- **Health check:** `GET /health`.
- **Metrics endpoint:** `GET /metrics`, backed by Micrometer's Prometheus registry.
- **Resilience example:** `GET /api/data` calls a simulated external operation through a Resilience4j circuit breaker.
- **Container image:** Java 21 runtime image with a non-root application user and a bounded heap setting.
- **Build configuration:** Gradle Java/application plugins, Shadow packaging, JaCoCo reporting, and JUnit 5 dependencies are declared.

## Architecture

```text
HTTP client
    |
    v
Javalin service (:8080)
    |             |                 |
 /health       /metrics         /api/data
                                  |
                           Circuit breaker
                                  |
                         Simulated dependency
```

The simulated dependency deliberately produces intermittent failures so the circuit-breaker path can be explored without relying on a real third-party service. The returned payload is demonstration data, not a live telemetry feed.

## Technology stack

| Area | Technology |
|---|---|
| Language/runtime | Java 21 |
| HTTP | Javalin |
| Resilience | Resilience4j CircuitBreaker |
| Metrics | Micrometer + Prometheus registry |
| Logging | SLF4J |
| Build | Gradle |
| Testing | JUnit 5 / JaCoCo |
| Container | Docker, Eclipse Temurin JRE |

## Current repository layout

The Java application and test now use Gradle's standard source layout. The repository uses the Gradle 8.10.2 toolchain in CI and Java 21 for compilation and runtime.

The CI workflow builds the application, runs deterministic unit tests, generates JaCoCo coverage output, and packages the executable Shadow JAR. A Gradle Wrapper is not currently checked in, so local builds require a compatible Gradle installation:

```bash
gradle clean test jacocoTestReport shadowJar
```

The circuit-breaker test verifies that four consecutive failures open the configured four-call window and that subsequent calls are rejected while open. The external dependency remains simulated; the project does not connect to a live telemetry service or claim production reliability.

## Next engineering steps

- Add an injectable HTTP client and endpoint-level tests using a deterministic stub server.
- Add container image build and runtime health checks to CI.
- Add a Gradle Wrapper after generating and verifying it with the selected Gradle distribution.
- Add deterministic metrics assertions for request counts and circuit-breaker state.

## Run (after the source layout and build setup are corrected)

With a compatible Gradle installation and dependencies available:

```bash
gradle clean build
```

The intended packaged artifact name is `orbit-telemetry.jar`. The current checked-in layout has the gaps listed above, so successful packaging should be verified rather than assumed.

## License

See the repository for license information.
