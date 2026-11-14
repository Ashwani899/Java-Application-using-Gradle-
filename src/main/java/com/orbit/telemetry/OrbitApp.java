package com.orbit.telemetry;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.javalin.Javalin;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

public class OrbitApp {
    private static final Logger log = LoggerFactory.getLogger(OrbitApp.class);

    static CircuitBreaker createCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowSize(4)
            .minimumNumberOfCalls(4)
            .build();
        return CircuitBreaker.of("externalService", config);
    }

    static String simulateExternalCall(double sample) {
        if (sample > 0.7) {
            throw new RuntimeException("Simulated external service timeout!");
        }
        return "{\"data\": \"Telemetry active\", \"signals\": 42}";
    }

    private static String simulateExternalCall() {
        return simulateExternalCall(Math.random());
    }

    public static void main(String[] args) {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        CircuitBreaker circuitBreaker = createCircuitBreaker();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        Javalin app = Javalin.create(config -> config.showJavalinBanner = false).start(port);

        app.get("/health", ctx -> ctx.json(Map.of("status", "UP")));
        app.get("/metrics", ctx -> {
            ctx.contentType("text/plain; version=0.0.4; charset=utf-8");
            ctx.result(registry.scrape());
        });

        app.get("/api/data", ctx -> {
            try {
                String result = circuitBreaker.executeSupplier(OrbitApp::simulateExternalCall);
                ctx.contentType("application/json");
                ctx.result(result);
            } catch (CallNotPermittedException e) {
                ctx.status(503).json(Map.of("error", "Service unavailable: circuit breaker is open"));
            } catch (RuntimeException e) {
                ctx.status(503).json(Map.of("error", "Service unavailable"));
            }
        });

        log.info("Orbit Telemetry Gateway started on port {}", port);
    }
}
