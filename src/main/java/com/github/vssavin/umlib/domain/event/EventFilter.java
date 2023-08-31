package com.github.vssavin.umlib.domain.event;

import java.util.Date;

/**
 * Provides storage of event filtering params.
 *
 * @author vssavin on 31.08.2023
 */
public class EventFilter {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private Date startEventTimestamp;
    private Date endEventTimestamp;

    public EventFilter(Long eventId, Long userId, EventType eventType, Date startEventTimestamp, Date endEventTimestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.startEventTimestamp = startEventTimestamp;
        this.endEventTimestamp = endEventTimestamp;
    }

    public static EventFilter emptyEventFilter() {
        return new EventFilter(null, null, null, null, null);
    }

    public boolean isEmpty() {
        return eventId == null && userId == null && eventType == null &&
                startEventTimestamp == null && endEventTimestamp == null;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Date getStartEventTimestamp() {
        return startEventTimestamp;
    }

    public Date getEndEventTimestamp() {
        return endEventTimestamp;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setStartEventTimestamp(Date startEventTimestamp) {
        this.startEventTimestamp = startEventTimestamp;
    }

    public void setEndEventTimestamp(Date endEventTimestamp) {
        this.endEventTimestamp = endEventTimestamp;
    }

    @Override
    public String toString() {
        return "EventFilter{" +
                "eventId=" + eventId +
                ", userId=" + userId +
                ", eventType=" + eventType +
                ", startEventTimestamp=" + startEventTimestamp +
                ", endEventTimestamp=" + endEventTimestamp +
                '}';
    }
}
