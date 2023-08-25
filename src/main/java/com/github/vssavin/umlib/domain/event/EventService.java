package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.base.repository.RepositoryOptionalFunction;
import com.github.vssavin.umlib.base.repository.UmRepositorySupport;
import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author vssavin on 24.08.2023
 */
@Service
public class EventService {
    private final UmRepositorySupport<EventRepository, Event> repositorySupport;
    private final DataSourceSwitcher dataSourceSwitcher;
    private final TransactionalEventService transactionalService;

    @Autowired
    public EventService(EventRepository eventRepository, DataSourceSwitcher dataSourceSwitcher,
                        TransactionalEventService transactionalService) {
        this.dataSourceSwitcher = dataSourceSwitcher;
        this.transactionalService = transactionalService;
        this.repositorySupport = new UmRepositorySupport<>(eventRepository, dataSourceSwitcher);
    }

    public Event createEvent(User user, EventType eventType, String eventMessage) {
        Event event = new Event(user.getId(), eventType, new Timestamp(System.currentTimeMillis()), eventMessage, user);
        RepositoryOptionalFunction<EventRepository, Event> function = repo -> Optional.of(repo.save(event));
        Optional<Event> optional =
                repositorySupport.execute(function, "Error occurred while saving event entity!");
        if (optional.isPresent()) return optional.get();
        throw new CreateEventException("The database did not return an event entity!");
    }

    public List<Event> findAllEvents() {
        dataSourceSwitcher.switchToUmDataSource();
        List<Event> list = transactionalService.findAllEvents();
        dataSourceSwitcher.switchToPreviousDataSource();
        return list;
    }

    public List<User> findAllUsers() {
        dataSourceSwitcher.switchToUmDataSource();
        List<User> list = transactionalService.findAllUsers();
        dataSourceSwitcher.switchToPreviousDataSource();
        return list;
    }
}
