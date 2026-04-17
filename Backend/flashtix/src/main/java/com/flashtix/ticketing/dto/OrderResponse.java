package com.flashtix.ticketing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long orderId;
    private String eventName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private String downloadUrl; // We will generate this dynamically
}