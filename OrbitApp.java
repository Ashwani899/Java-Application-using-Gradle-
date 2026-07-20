package com.orbit.telemetry;

import io.javalin.Javalin;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

public class OrbitApp {
    private static final Logger log = LoggerFactory.getLogger(OrbitApp.class);

    public static void main(String[] args) {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50).waitDurationInOpenState(Duration.ofSeconds(10)).slidingWindowSize(4).build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("externalService", cbConfig);

        Javalin app = Javalin.create(config -> { config.showJavalinBanner = false; }).start(8080);

        app.get("/health", ctx -> ctx.json("{\"status\": \"UP\"}"));
        app.get("/metrics", ctx -> { ctx.contentType("text/plain"); ctx.result(registry.scrape()); });
        
        app.get("/api/data", ctx -> {
            try {
                String result = circuitBreaker.executeSupplier(OrbitApp::simulateExternalCall);
                ctx.result(result);
            } catch (Exception e) {
                ctx.status(503).result("{\"error\": \"Service Unavailable (Circuit Open)\"}");
            }
        });
        log.info("Orbit Telemetry Gateway started on port 8080 (Java 21)");
    }

    private static String simulateExternalCall() {
        if (Math.random() > 0.7) throw new RuntimeException("Simulated external service timeout!");
        return "{\"data\": \"Telemetry active\", \"signals\": 42}";
    }
}
