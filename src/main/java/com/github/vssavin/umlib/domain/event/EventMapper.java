package com.github.vssavin.umlib.domain.event;

import org.mapstruct.Mapper;

/**
 * @author vssavin on 29.08.2023
 */
@Mapper(componentModel = "spring")
interface EventMapper {
    EventDto toDto(Event user);
    Event toEntity(EventDto userDto);
}
