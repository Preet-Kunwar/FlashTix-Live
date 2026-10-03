package com.flashtix.events.repository;

import com.flashtix.events.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * FIX: Race condition — ticket overselling under concurrent Kafka consumers.
     *
     * Without pessimistic locking, two consumers can both read availableTickets=1,
     * both pass the `>= quantity` check, and both deduct, resulting in -1 tickets.
     *
     * This query acquires a database-level row lock (SELECT ... FOR UPDATE), ensuring
     * only one Kafka consumer can process the ticket deduction at a time per event.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}