package com.orbit.telemetry;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrbitAppTest {
    @Test
    void simulatedExternalCallReturnsTelemetryForHealthySample() {
        assertEquals(
            "{\"data\": \"Telemetry active\", \"signals\": 42}",
            OrbitApp.simulateExternalCall(0.5)
        );
    }

    @Test
    void simulatedExternalCallThrowsForFailureSample() {
        assertThrows(RuntimeException.class, () -> OrbitApp.simulateExternalCall(0.8));
    }

    @Test
    void circuitBreakerOpensAfterFourConsecutiveFailures() {
        CircuitBreaker circuitBreaker = OrbitApp.createCircuitBreaker();

        for (int i = 0; i < 4; i++) {
            assertThrows(
                RuntimeException.class,
                () -> circuitBreaker.executeSupplier(() -> {
                    throw new RuntimeException("simulated failure");
                })
            );
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertThrows(
            CallNotPermittedException.class,
            () -> circuitBreaker.executeSupplier(() -> "should not execute")
        );
    }
}
