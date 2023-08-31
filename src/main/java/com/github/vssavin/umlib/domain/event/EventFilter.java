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
    private Date eventTimestamp;
    private String eventMessage;

    public EventFilter(Long eventId, Long userId, EventType eventType, Date eventTimestamp, String eventMessage) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
        this.eventMessage = eventMessage;
    }

    public static EventFilter emptyEventFilter() {
        return new EventFilter(null, null, null, null, null);
    }

    public boolean isEmpty() {
        return eventId == null && userId == null && eventType == null && eventTimestamp == null && eventMessage == null;
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

    public Date getEventTimestamp() {
        return eventTimestamp;
    }

    public String getEventMessage() {
        return eventMessage;
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

    public void setEventTimestamp(Date eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    @Override
    public String toString() {
        return "EventFilter{" +
                "eventId=" + eventId +
                ", userId=" + userId +
                ", eventType=" + eventType +
                ", eventTimestamp=" + eventTimestamp +
                ", eventMessage='" + eventMessage + '\'' +
                '}';
    }
}
