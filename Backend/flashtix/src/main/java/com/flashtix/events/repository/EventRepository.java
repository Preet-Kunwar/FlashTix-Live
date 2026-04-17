package com.flashtix.events.repository;

import com.flashtix.events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Spring Data JPA automatically implements this!
}