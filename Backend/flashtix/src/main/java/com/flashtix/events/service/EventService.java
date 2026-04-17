package com.flashtix.events.service;

import com.flashtix.events.entity.Event;
import com.flashtix.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

//    @Cacheable("eventsCatalog")
    public List<Event> getAllUpcomingEvents() {
        // In a real app, you'd filter by date, but we will grab all for testing
        return eventRepository.findAll();
    }

    public Event createEvent(Event event) {
        // Ensure available tickets equals total tickets on creation
        event.setAvailableTickets(event.getTotalTickets());
        return eventRepository.save(event);
    }
}