package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.domain.user.UserDto;

import java.util.Date;

/**
 * Base user event data transfer object.
 *
 * @author vssavin on 29.08.2023
 */
public class EventDto {

	private Long id;

	private Long userId;

	private EventType eventType;

	private Date eventTimestamp;

	private String eventMessage;

	private UserDto user;

	public Long getId() {
		return id;
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

	public UserDto getUser() {
		return user;
	}

	public void setId(Long id) {
		this.id = id;
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

	public void setUser(UserDto user) {
		this.user = user;
	}

}
