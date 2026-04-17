package com.flashtix.ticketing.service;

import com.flashtix.common.dto.OrderEvent;
import com.flashtix.events.entity.Event;
import com.flashtix.events.repository.EventRepository;
import com.flashtix.ticketing.entity.Order;
import com.flashtix.ticketing.repository.OrderRepository;
import com.flashtix.users.entity.User;
import com.flashtix.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // Injects the WebSocket messaging template
    private final SimpMessagingTemplate messagingTemplate;

    // Injects Redis for lightning-fast caching
    private final RedisTemplate<String, Object> redisTemplate;


    private final PaymentService paymentService;


    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @KafkaListener(topics = "order_topic", groupId = "flashtix-ticketing-group")
    @Transactional
    public void consumeOrder(OrderEvent eventDto) {
        System.out.println("Processing order from Kafka: " + eventDto);

        Event event = eventRepository.findById(eventDto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getAvailableTickets() >= eventDto.getQuantity()) {


            BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(eventDto.getQuantity()));

            // --- NEW: Process the Payment before confirming the order! ---
            boolean paymentSuccess = paymentService.processPayment(eventDto.getUserId(), totalAmount);

            if (!paymentSuccess) {
                System.out.println("Order cancelled for user " + eventDto.getUserId() + " due to payment failure.");
                return; // Stop processing the order!
            }
            // -------------------------------------------------------------

            // 1. Update the database


            int newTicketCount = event.getAvailableTickets() - eventDto.getQuantity();
            event.setAvailableTickets(newTicketCount);
            eventRepository.save(event);

            User user = userRepository.findById(eventDto.getUserId()).get();

            Order order = new Order();
            order.setUser(user);
            order.setEvent(event);
            order.setTicketQuantity(eventDto.getQuantity());
            order.setTotalAmount(event.getTicketPrice().multiply(BigDecimal.valueOf(eventDto.getQuantity())));
            order.setStatus("CONFIRMED");
            order.setOrderDate(LocalDateTime.now());
            orderRepository.save(order);

            // 2. Update Redis Cache (Key: "event:{id}:tickets")
            String redisKey = "event:" + event.getId() + ":tickets";
            redisTemplate.opsForValue().set(redisKey, newTicketCount);

            // 3. Broadcast the live update via WebSockets to the React frontend!
            // Anyone subscribed to /topic/events/{id} will instantly receive the new count.
            messagingTemplate.convertAndSend("/topic/events/" + event.getId(), newTicketCount);
// Tell RabbitMQ to generate the PDF in the background
            rabbitTemplate.convertAndSend("ticketing_exchange", "pdf_routing_key", order.getId());
            System.out.println("Live update broadcasted: " + newTicketCount + " tickets remaining for Event " + event.getId());
        } else {
            System.out.println("Order failed: Not enough tickets available.");
        }
    }
}