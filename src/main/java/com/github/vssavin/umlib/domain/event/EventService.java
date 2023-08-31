package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.base.repository.PagedRepositoryFunction;
import com.github.vssavin.umlib.base.repository.UmRepositorySupport;
import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.data.pagination.Paging;
import com.github.vssavin.umlib.domain.user.User;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Main implementation of event service.
 *
 * @author vssavin on 24.08.2023
 */
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final UmRepositorySupport<EventRepository, EventDto> repositorySupport;

    @Autowired
    public EventService(DataSourceSwitcher dataSourceSwitcher,
                        EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.repositorySupport = new UmRepositorySupport<>(eventRepository, dataSourceSwitcher);
    }

    public EventDto createEvent(User user, EventType eventType, String eventMessage) {
        Event event = new Event(user.getId(), eventType, new Timestamp(System.currentTimeMillis()), eventMessage, user);
        user.getEvents().add(event);
        int size = user.getEvents().size();
        return eventMapper.toDto(user.getEvents().get(size - 1));
    }

    @Transactional
    public Paged<EventDto> findEvents(EventFilter eventFilter, int pageNumber, int pageSize) {
        String message = "Error while search events with params: pageNumber = %d, pageSize = %d, filter: [%s]!";
        Object[] params = {pageNumber, pageSize, eventFilter};
        PagedRepositoryFunction<EventRepository, EventDto> function;
        Pageable pageable;
        try {
            pageable = PageRequest.of(pageNumber - 1, pageSize);
        } catch (Exception e) {
            throw new EventServiceException(String.format(message, params), e);
        }

        if (eventFilter == null || eventFilter.isEmpty()) {
            function = (repository -> {
                Page<Event> list = eventRepository.findAll(pageable);
                return list.map(eventMapper::toDto);
            });
        } else {
            Predicate predicate = eventFilterToPredicate(eventFilter);
            function = (repository -> {
                Page<Event> list = eventRepository.findAll(predicate, pageable);
                return list.map(eventMapper::toDto);
            });
        }

        Page<EventDto> events = repositorySupport.execute(function, message, params);

        return new Paged<>(events, Paging.of(events.getTotalPages(), pageNumber, pageSize));
    }

    private Predicate eventFilterToPredicate(EventFilter eventFilter) {
        QEvent event = QEvent.event;
        BooleanExpression expression = null;
        List<BooleanExpression> expressions = new ArrayList<>();
        if (eventFilter.getEventId() != null) {
             expressions.add(event.id.eq(eventFilter.getEventId()));
        }

        if (eventFilter.getUserId() != null) {
            expressions.add(event.userId.eq(eventFilter.getUserId()));
        }

        if (eventFilter.getEventType() != null) {
            expressions.add(event.eventType.eq(eventFilter.getEventType()));
        }

        if (eventFilter.getStartEventTimestamp() != null) {
            expressions.add(event.eventTimestamp
                    .between(eventFilter.getStartEventTimestamp(), eventFilter.getEndEventTimestamp()));
        }

        for (BooleanExpression expr : expressions) {
            if (expression == null) {
                expression = expr;
            } else {
                expression = expression.eq(expr);
            }
        }

        return expression;
    }
}
