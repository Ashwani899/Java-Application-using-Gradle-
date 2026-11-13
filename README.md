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

The current repository contains `OrbitApp.java`, `OrbitAppTest.java`, `build.gradle`, `settings.gradle`, `Dockerfile`, and a pipeline definition at the repository root.

There are a few setup gaps to resolve before a clean clone is fully reproducible:

1. Gradle's default Java source layout is `src/main/java` and `src/test/java`, but the Java files are currently at the repository root.
2. A Gradle Wrapper (`gradlew`, `gradlew.bat`, and `gradle/wrapper/`) is not currently checked in.
3. The pipeline YAML is named `pipeline.yml` at the root; GitHub Actions will not discover it there. A GitHub Actions workflow belongs under `.github/workflows/`.
4. The current test contains a placeholder `assertTrue(true)`; it does not yet verify endpoint behavior, circuit-breaker transitions, or metrics output.

These items are documented so that the build status is not overstated.

## Next engineering steps

- Move the application and test classes into Gradle's conventional source directories, or explicitly configure source sets.
- Add a Gradle Wrapper and pin the wrapper distribution.
- Move and validate the CI workflow under `.github/workflows/`.
- Replace the placeholder test with endpoint and resilience behavior tests.
- Add a deterministic injectable external-service stub instead of relying on random failures in tests.
- Add a `docker build` smoke test and document the runtime configuration.

## Run (after the source layout and build setup are corrected)

With a compatible Gradle installation and dependencies available:

```bash
gradle clean build
```

The intended packaged artifact name is `orbit-telemetry.jar`. The current checked-in layout has the gaps listed above, so successful packaging should be verified rather than assumed.

## License

See the repository for license information.
