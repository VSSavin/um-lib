package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.domain.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper to convert between event entity and event dto.
 *
 * @author vssavin on 29.08.2023
 */
@Mapper(componentModel = "spring")
interface EventMapper {

	UserMapper userMapper = Mappers.getMapper(UserMapper.class);

	@Mapping(target = "user", expression = "java(userMapper.toDto(event.getUser()))")
	EventDto toDto(Event event);

	@Mapping(target = "user", expression = "java(userMapper.toEntity(eventDto.getUser()))")
	Event toEntity(EventDto eventDto);

}
