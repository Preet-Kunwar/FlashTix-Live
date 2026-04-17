package com.flashtix.ticketing.service;

import com.flashtix.common.dto.OrderEvent;
import com.flashtix.ticketing.dto.OrderResponse;
import com.flashtix.ticketing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC = "order_topic";
    private final OrderRepository orderRepository;

    public void sendOrderToQueue(Long userId, Long eventId, Integer quantity) {
        OrderEvent event = new OrderEvent(userId, eventId, quantity);
        // This is non-blocking and very fast!
        kafkaTemplate.send(TOPIC, event);
    }

    // You will need to import com.flashtix.ticketing.dto.OrderResponse;
    // You will need to import java.util.List;
    // You will need to import java.util.stream.Collectors;

    public List<OrderResponse> getMyTickets(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(order -> {
            OrderResponse dto = new OrderResponse();
            dto.setOrderId(order.getId());
            dto.setEventName(order.getEvent().getName());
            dto.setQuantity(order.getTicketQuantity());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setOrderDate(order.getOrderDate());
            // Create a dynamic link to our download API
            dto.setDownloadUrl("/api/orders/" + order.getId() + "/download");
            return dto;
        }).collect(Collectors.toList());
    }
}

















//------------------------------------------------------------------------------


//package com.flashtix.ticketing.service;



//
//import com.flashtix.events.entity.Event;
//import com.flashtix.events.repository.EventRepository;
//import com.flashtix.ticketing.entity.Order;
//import com.flashtix.ticketing.repository.OrderRepository;
//import com.flashtix.users.entity.User;
//import com.flashtix.users.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class TicketingService {
//
//    private final EventRepository eventRepository;
//    private final OrderRepository orderRepository;
//    private final UserRepository userRepository;
//
//    // @Transactional ensures that if anything fails, the database rolls back completely
//    @Transactional
//    public Order purchaseTicket(Long userId, Long eventId, Integer quantity) {
//
//        // 1. Fetch the User and Event from the database
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new RuntimeException("Event not found"));
//
//        // 2. Check if there are enough tickets available
//        if (event.getAvailableTickets() < quantity) {
//            throw new RuntimeException("Not enough tickets available for this event.");
//        }
//
//        // 3. Deduct the tickets
//        event.setAvailableTickets(event.getAvailableTickets() - quantity);
//        eventRepository.save(event); // Update the database
//
//        // 4. Calculate total price and create the Order
//        BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(quantity));
//
//        Order newOrder = new Order();
//        newOrder.setUser(user);
//        newOrder.setEvent(event);
//        newOrder.setTicketQuantity(quantity);
//        newOrder.setTotalAmount(totalAmount);
//        newOrder.setStatus("CONFIRMED");
//        newOrder.setOrderDate(LocalDateTime.now());
//
//        // 5. Save the order to the database
//        return orderRepository.save(newOrder);
//    }
//}