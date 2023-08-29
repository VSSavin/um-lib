package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vssavin on 24.08.2023
 */
@Service
public class EventService {
    private final DataSourceSwitcher dataSourceSwitcher;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Autowired
    public EventService(DataSourceSwitcher dataSourceSwitcher, EventRepository eventRepository, EventMapper eventMapper) {
        this.dataSourceSwitcher = dataSourceSwitcher;
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    public EventDto createEvent(User user, EventType eventType, String eventMessage) {
        Event event = new Event(user.getId(), eventType, new Timestamp(System.currentTimeMillis()), eventMessage, user);
        user.getEvents().add(event);
        int size = user.getEvents().size();
        return eventMapper.toDto(user.getEvents().get(size - 1));
    }

    public List<EventDto> findAllEvents() {
        dataSourceSwitcher.switchToUmDataSource();
        List<Event> list = eventRepository.findAll();
        List<EventDto> dtoList = list.stream().map(eventMapper::toDto).collect(Collectors.toList());
        dataSourceSwitcher.switchToPreviousDataSource();
        return dtoList;
    }
}
