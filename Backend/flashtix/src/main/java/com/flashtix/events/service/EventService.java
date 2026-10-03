package com.flashtix.events.service;

import com.flashtix.events.entity.Event;
import com.flashtix.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Returns all events.
     */
    public List<Event> getAllUpcomingEvents() {
        log.info("[EVENTS] Fetching all events from database");
        List<Event> events = eventRepository.findAll();
        log.info("[EVENTS] ✅ Loaded {} event(s) from database", events.size());
        return events;
    }

    /**
     * Creates a new event.
     */
    public Event createEvent(Event event) {
        // Always set availableTickets = totalTickets on creation
        event.setAvailableTickets(event.getTotalTickets());

        Event saved = eventRepository.save(event);
        log.info("[EVENTS] ✅ Event created — id={}, name='{}', totalTickets={}, price={}, flashSaleStart={}",
                saved.getId(), saved.getName(), saved.getTotalTickets(),
                saved.getTicketPrice(), saved.getFlashSaleStartTime());
        log.info("[EVENTS] Cache 'eventsCatalog' evicted — next GET will refresh from database");

        return saved;
    }
}