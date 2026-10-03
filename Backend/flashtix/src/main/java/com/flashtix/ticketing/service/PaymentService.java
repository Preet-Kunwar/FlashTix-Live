package com.flashtix.ticketing.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentService {

    /**
     * Simulates a third-party payment gateway call.
     *
     * Failure rate: ~5% (1 in 20 requests) — realistic for production.
     * This is intentionally kept non-zero to demonstrate the Circuit Breaker pattern.
     * The Resilience4j CircuitBreaker opens when failure rate exceeds 50% in a
     * 10-request sliding window, then auto-recovers after 10 seconds.
     *
     * To test the Circuit Breaker manually: change the threshold below to 0.1
     * and buy tickets rapidly — the circuit will open after a few failures.
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public boolean processPayment(Long userId, BigDecimal amount) {
        log.info("[PAYMENT] Attempting charge — userId={}, amount=${}", userId, amount);

        // Simulate a third-party API that fails ~5% of the time
        // Change to > 0.1 to increase failure rate for circuit breaker testing
        if (Math.random() > 0.95) {
            log.warn("[PAYMENT] Simulated gateway timeout (~5% chance) — throwing to trigger Circuit Breaker");
            throw new RuntimeException("Third-Party Payment Gateway Timeout!");
        }

        log.info("[PAYMENT] ✅ Payment SUCCESSFUL — userId={}, amount=${}", userId, amount);
        return true;
    }

    /**
     * Circuit Breaker fallback — fires when:
     *   (a) processPayment() throws an exception, OR
     *   (b) The circuit is OPEN (too many recent failures)
     */
    public boolean paymentFallback(Long userId, BigDecimal amount, Throwable throwable) {
        log.error("[PAYMENT] ⚡ CIRCUIT BREAKER TRIGGERED for userId={}, amount=${} — Reason: {}",
                userId, amount, throwable.getMessage());
        return false;
    }
}