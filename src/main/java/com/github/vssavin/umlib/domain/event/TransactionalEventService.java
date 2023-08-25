package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vssavin on 24.08.2023
 */
@Service
@Transactional
public class TransactionalEventService {
    private final EventRepository eventRepository;

    public TransactionalEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<User> findAllUsers() {
        List<Event> eventList = eventRepository.findAll();
        List<User> users = new ArrayList<>();
        eventList.forEach(event -> users.add(event.getUser()));
        return users;
    }

    public List<Event> findAllEvents() {
        List<Event> eventList = eventRepository.findAll();
        eventList.forEach(evetn -> evetn.getUser().getLogin());
        return eventList;
    }
}
