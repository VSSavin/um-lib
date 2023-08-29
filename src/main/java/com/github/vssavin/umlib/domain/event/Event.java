package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.domain.user.User;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * @author vssavin on 24.08.2023
 */
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @Column(name = "event_timestamp")
    private Date eventTimestamp;

    @Column(name = "event_message")
    private String eventMessage;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public Event(Long id, Long userId, Date eventTimestamp, String eventMessage) {
        this.id = id;
        this.userId = userId;
        this.eventTimestamp = eventTimestamp;
        this.eventMessage = eventMessage;
    }

    public Event(Long userId, EventType eventType, Date eventTimestamp, String eventMessage, User user) {
        this.userId = userId;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
        this.eventMessage = eventMessage;
        this.user = user;
    }

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Date getEventTimestamp() {
        return eventTimestamp;
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public EventType getEventType() {
        return eventType;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEventTimestamp(Date eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Event event = (Event) o;

        if (!Objects.equals(id, event.id)) {
            return false;
        }
        if (!userId.equals(event.userId)) {
            return false;
        }
        if (!eventTimestamp.equals(event.eventTimestamp)) {
            return false;
        }
        return eventMessage.equals(event.eventMessage);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + userId.hashCode();
        result = 31 * result + eventTimestamp.hashCode();
        result = 31 * result + eventMessage.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventTimestamp=" + eventTimestamp +
                ", eventMessage='" + eventMessage + '\'' +
                '}';
    }
}
