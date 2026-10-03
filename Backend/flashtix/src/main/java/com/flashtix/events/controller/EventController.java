package com.flashtix.events.controller;

import com.flashtix.events.entity.Event;
import com.flashtix.events.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * GET /api/events — Returns all events. Public endpoint, no auth required.
     */
    @GetMapping
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        log.debug("[EVENT-CTRL] GET /api/events — fetching event catalog");
        List<Event> events = eventService.getAllUpcomingEvents();
        log.debug("[EVENT-CTRL] Returning {} event(s)", events.size());
        return ResponseEntity.ok(events);
    }

    /**
     * POST /api/events — Creates a new event. Requires ROLE_ADMIN.
     * Protected by both @PreAuthorize (method level) and SecurityConfig (URL level).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(@RequestBody Event event, Authentication authentication) {
        log.info("[EVENT-CTRL] POST /api/events — admin='{}' creating event '{}'",
                authentication.getName(), event.getName());
        Event created = eventService.createEvent(event);
        log.info("[EVENT-CTRL] ✅ Event created — id={}, name='{}'", created.getId(), created.getName());
        return ResponseEntity.ok(created);
    }
}