package com.github.vssavin.umlib.domain.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vssavin on 24.08.2023
 */
@RestController
@RequestMapping(EventController.EVENT_CONTROLLER_PATH)
public class EventController {
    static final String EVENT_CONTROLLER_PATH = "/um/events";

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<Iterable<Event>> getAllEvents() {
        return new ResponseEntity<>(eventService.findAllEvents(), HttpStatus.OK);
    }

}
