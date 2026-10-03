package com.flashtix.events.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Coldplay World Tour"

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private LocalDateTime flashSaleStartTime;

    @Column(nullable = false)
    private Integer totalTickets;

    @Column(nullable = false)
    private Integer availableTickets;

    @Column(nullable = false)
    private BigDecimal ticketPrice;
}