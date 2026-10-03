package com.flashtix.ticketing.controller;

import com.flashtix.common.service.FileStorageService;
import com.flashtix.ticketing.dto.OrderResponse;
import com.flashtix.ticketing.entity.Order;
import com.flashtix.ticketing.repository.OrderRepository;
import com.flashtix.ticketing.service.TicketingService;
import com.flashtix.users.entity.User;
import com.flashtix.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TicketingService ticketingService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    /**
     * POST /api/orders/purchase — Queues a purchase order to Kafka.
     * Returns HTTP 202 immediately. Actual ticket deduction is async.
     */
    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTicket(
            @RequestParam Long eventId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("[ORDER-CTRL] Purchase request — user='{}', eventId={}, quantity={}", username, eventId, quantity);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[ORDER-CTRL] ❌ Authenticated user '{}' not found in database", username);
                    return new RuntimeException("Authenticated user not found: " + username);
                });

        ticketingService.sendOrderToQueue(user.getId(), eventId, quantity);

        log.info("[ORDER-CTRL] ✅ Order queued — returning HTTP 202 to user='{}'", username);
        return ResponseEntity.accepted()
                .body("Order received! You are in the queue. We are processing your ticket.");
    }

    /**
     * GET /api/orders/my-tickets — Returns all orders for the authenticated user.
     */
    @GetMapping("/my-tickets")
    public ResponseEntity<List<OrderResponse>> getMyTickets(Authentication authentication) {
        String username = authentication.getName();
        log.info("[ORDER-CTRL] Fetching tickets for user='{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<OrderResponse> tickets = ticketingService.getMyTickets(user.getId());
        log.info("[ORDER-CTRL] Returning {} ticket(s) for user='{}'", tickets.size(), username);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET /api/orders/{orderId}/download — Streams a PDF ticket to the browser.
     * Validates that the requesting user owns the order before streaming.
     */
    @GetMapping("/{orderId}/download")
    public ResponseEntity<InputStreamResource> downloadTicket(
            @PathVariable Long orderId,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("[ORDER-CTRL] PDF download request — user='{}', orderId={}", username, orderId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("[ORDER-CTRL] ❌ Order not found — orderId={}", orderId);
                    return new RuntimeException("Order not found: " + orderId);
                });

        // Security: ensure the user owns the order they're trying to download
        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("[ORDER-CTRL] 🚫 FORBIDDEN — user='{}' attempted to download orderId={} owned by userId={}",
                    username, orderId, order.getUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"CONFIRMED".equals(order.getStatus())) {
            log.warn("[ORDER-CTRL] ❌ Download refused — orderId={} status is '{}', not CONFIRMED",
                    orderId, order.getStatus());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String fileName = "ticket_order_" + orderId + ".pdf";
        log.info("[ORDER-CTRL] Streaming '{}' from S3 storage", fileName);

        InputStream fileStream = fileStorageService.downloadTicketPdf(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        log.info("[ORDER-CTRL] ✅ PDF stream started for user='{}', orderId={}", username, orderId);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(fileStream));
    }
}