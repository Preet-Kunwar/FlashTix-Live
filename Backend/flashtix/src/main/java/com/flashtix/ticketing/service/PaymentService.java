package com.flashtix.ticketing.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    // The name matches the instance we defined in application.yml
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public boolean processPayment(Long userId, BigDecimal amount) {
        System.out.println("Attempting to charge $" + amount + " for user " + userId + "...");

        // Simulate a Third-Party API that fails 30% of the time
        if (Math.random() > 0.7) {
            throw new RuntimeException("Third-Party Payment Gateway Timeout!");
        }

        System.out.println("Payment Successful!");
        return true;
    }

    // This runs INSTANTLY if the API throws an error, or if the Circuit is "Open"
    public boolean paymentFallback(Long userId, BigDecimal amount, Throwable throwable) {
        System.out.println("CIRCUIT BREAKER TRIGGERED! Fallback activated for user: " + userId);
        System.out.println("Reason: " + throwable.getMessage());
        return false; // Return false so we know the payment failed
    }
}