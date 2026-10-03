package com.flashtix.ticketing.service;

import com.flashtix.common.dto.OrderEvent;
import com.flashtix.common.config.RabbitMQConfig;
import com.flashtix.events.entity.Event;
import com.flashtix.events.repository.EventRepository;
import com.flashtix.ticketing.entity.Order;
import com.flashtix.ticketing.repository.OrderRepository;
import com.flashtix.users.entity.User;
import com.flashtix.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Kafka consumer — processes each purchase order asynchronously.
     *
     * Flow:
     *  1. Acquire DB-level pessimistic lock on the event row (prevents overselling)
     *  2. Check ticket availability
     *  3. Process payment (via PaymentService with Circuit Breaker)
     *  4. Deduct tickets from DB
     *  5. Save confirmed Order
     *  6. Update Redis cache
     *  7. Broadcast live ticket count via WebSocket
     *  8. Trigger PDF generation via RabbitMQ
     */
    @KafkaListener(topics = "order_topic", groupId = "flashtix-ticketing-group")
    @Transactional
    public void consumeOrder(OrderEvent eventDto) {
        log.info("[ORDER-CONSUMER] ▶ Received order from Kafka — userId={}, eventId={}, quantity={}",
                eventDto.getUserId(), eventDto.getEventId(), eventDto.getQuantity());

        // ── 1. Load Event with pessimistic write lock (prevents overselling) ──────
        Event event = eventRepository.findByIdForUpdate(eventDto.getEventId())
                .orElseThrow(() -> {
                    log.error("[ORDER-CONSUMER] ❌ Event not found — eventId={}", eventDto.getEventId());
                    return new RuntimeException("Event not found: " + eventDto.getEventId());
                });

        User user = userRepository.findById(eventDto.getUserId())
                .orElseThrow(() -> {
                    log.error("[ORDER-CONSUMER] ❌ User not found — userId={}", eventDto.getUserId());
                    return new RuntimeException("User not found: " + eventDto.getUserId());
                });

        log.debug("[ORDER-CONSUMER] Loaded event='{}', availableTickets={}, requested={}",
                event.getName(), event.getAvailableTickets(), eventDto.getQuantity());

        // ── 2. Check ticket availability ──────────────────────────────────────────
        if (event.getAvailableTickets() < eventDto.getQuantity()) {
            log.warn("[ORDER-CONSUMER] ❌ SOLD OUT — event='{}' has {} tickets, user requested {}",
                    event.getName(), event.getAvailableTickets(), eventDto.getQuantity());

            BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(eventDto.getQuantity()));
            saveOrder(user, event, eventDto.getQuantity(), totalAmount, "FAILED");
            return;
        }

        // ── 3. Process payment ────────────────────────────────────────────────────
        BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(eventDto.getQuantity()));
        log.info("[ORDER-CONSUMER] Processing payment — userId={}, amount={}", eventDto.getUserId(), totalAmount);

        boolean paymentSuccess = paymentService.processPayment(eventDto.getUserId(), totalAmount);

        if (!paymentSuccess) {
            log.warn("[ORDER-CONSUMER] ❌ Payment REJECTED — userId={}, amount={}", eventDto.getUserId(), totalAmount);
            saveOrder(user, event, eventDto.getQuantity(), totalAmount, "FAILED");
            return;
        }

        log.info("[ORDER-CONSUMER] ✅ Payment ACCEPTED — userId={}, amount={}", eventDto.getUserId(), totalAmount);

        // ── 4. Deduct tickets (safe — inside DB lock transaction) ─────────────────
        int newTicketCount = event.getAvailableTickets() - eventDto.getQuantity();
        event.setAvailableTickets(newTicketCount);
        eventRepository.save(event);
        log.info("[ORDER-CONSUMER] Tickets deducted — event='{}' now has {} ticket(s) remaining",
                event.getName(), newTicketCount);

        // ── 5. Save confirmed order ───────────────────────────────────────────────
        Order order = saveOrder(user, event, eventDto.getQuantity(), totalAmount, "CONFIRMED");
        log.info("[ORDER-CONSUMER] ✅ Order CONFIRMED — orderId={}, user='{}', event='{}'",
                order.getId(), user.getUsername(), event.getName());

        // ── 6. Update Redis cache ─────────────────────────────────────────────────
        String redisKey = "event:" + event.getId() + ":tickets";
        redisTemplate.opsForValue().set(redisKey, newTicketCount);
        log.debug("[ORDER-CONSUMER] Redis cache updated — key='{}', value={}", redisKey, newTicketCount);

        // ── 7. Push live WebSocket update to dashboard ────────────────────────────
        messagingTemplate.convertAndSend("/topic/events/" + event.getId(), newTicketCount);
        log.debug("[ORDER-CONSUMER] WebSocket broadcast sent → /topic/events/{} = {}",
                event.getId(), newTicketCount);

        // ── 8. Trigger async PDF ticket generation via RabbitMQ ───────────────────
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, order.getId());
        log.info("[ORDER-CONSUMER] PDF generation triggered — orderId={} sent to RabbitMQ queue='{}'",
                order.getId(), RabbitMQConfig.QUEUE);
    }

    private Order saveOrder(User user, Event event, Integer quantity, BigDecimal totalAmount, String status) {
        Order order = new Order();
        order.setUser(user);
        order.setEvent(event);
        order.setTicketQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        order.setOrderDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        log.debug("[ORDER-CONSUMER] Order saved — id={}, status={}", saved.getId(), status);
        return saved;
    }
}