package com.github.vssavin.umlib.domain.event;

import org.mapstruct.Mapper;

/**
 * Mapper to convert between event entity and event dto.
 *
 * @author vssavin on 29.08.2023
 */
@Mapper(componentModel = "spring")
interface EventMapper {
    EventDto toDto(Event event);
    Event toEntity(EventDto eventDto);
}
