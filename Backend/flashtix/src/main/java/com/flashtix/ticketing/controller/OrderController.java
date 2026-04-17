package com.flashtix.ticketing.controller;

import com.flashtix.common.service.FileStorageService;
import com.flashtix.ticketing.dto.OrderResponse;
import com.flashtix.ticketing.entity.Order;
import com.flashtix.ticketing.repository.OrderRepository;
import com.flashtix.ticketing.service.TicketingService;
import com.flashtix.users.entity.User;
import com.flashtix.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TicketingService ticketingService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTicket(
            @RequestParam Long eventId,
            @RequestParam Integer quantity,
            Authentication authentication) { // Gets user from JWT

        // 1. Get the username from the token
        String username = authentication.getName();

        // 2. Find the user ID in the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Secure context user not found"));

        // 3. SEND TO KAFKA (Non-blocking!)
        ticketingService.sendOrderToQueue(user.getId(), eventId, quantity);

        // 4. Return an immediate HTTP 202 (Accepted) response to the user
        return ResponseEntity.accepted().body("Order received! You are in the queue. We are processing your ticket.");
    }


    // --- 2. NEW: Get All My Tickets ---
    @GetMapping("/my-tickets")
    public ResponseEntity<List<OrderResponse>> getMyTickets(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        return ResponseEntity.ok(ticketingService.getMyTickets(user.getId()));
    }

    // --- 3. NEW: Download a Specific PDF ---
    @GetMapping("/{orderId}/download")
    public ResponseEntity<InputStreamResource> downloadTicket(
            @PathVariable Long orderId,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Order order = orderRepository.findById(orderId).orElseThrow();

        // Security Check: Make sure the person requesting the file is the person who bought it!
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        String fileName = "ticket_order_" + orderId + ".pdf";
        InputStream fileStream = fileStorageService.downloadTicketPdf(fileName);

        HttpHeaders headers = new HttpHeaders();
        // This header forces the browser to download the file instead of just displaying text
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(fileStream));
    }
}