package com.flashtix.ticketing.service;

import com.flashtix.common.dto.OrderEvent;
import com.flashtix.ticketing.dto.OrderResponse;
import com.flashtix.ticketing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketingService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String ORDER_TOPIC = "order_topic";
    private final OrderRepository orderRepository;

    /**
     * Publishes an order event to Kafka for async processing.
     * Returns immediately (HTTP 202 Accepted) — the actual ticket deduction
     * and confirmation happens in OrderConsumer on the other side of the queue.
     */
    public void sendOrderToQueue(Long userId, Long eventId, Integer quantity) {
        log.info("[TICKETING] Queuing order → userId={}, eventId={}, quantity={}", userId, eventId, quantity);

        OrderEvent event = new OrderEvent(userId, eventId, quantity);

        // Send to Kafka asynchronously with a callback for success/failure
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(ORDER_TOPIC, String.valueOf(eventId), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[TICKETING] ❌ Failed to queue order for userId={}, eventId={} — Kafka error: {}",
                        userId, eventId, ex.getMessage());
            } else {
                log.info("[TICKETING] ✅ Order queued to Kafka topic='{}', partition={}, offset={}",
                        ORDER_TOPIC,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * Fetches all orders for a given user, mapped to the OrderResponse DTO.
     */
    public List<OrderResponse> getMyTickets(Long userId) {
        log.info("[TICKETING] Fetching ticket history for userId={}", userId);

        List<OrderResponse> tickets = orderRepository.findByUserId(userId).stream()
                .map(order -> {
                    OrderResponse dto = new OrderResponse();
                    dto.setOrderId(order.getId());
                    dto.setEventName(order.getEvent().getName());
                    dto.setQuantity(order.getTicketQuantity());
                    dto.setTotalAmount(order.getTotalAmount());
                    dto.setStatus(order.getStatus());
                    dto.setOrderDate(order.getOrderDate());
                    dto.setDownloadUrl("/api/orders/" + order.getId() + "/download");
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("[TICKETING] Found {} ticket(s) for userId={}", tickets.size(), userId);
        return tickets;
    }
}