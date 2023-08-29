package com.github.vssavin.umlib.domain.user;

import org.mapstruct.Mapper;

/**
 * @author vssavin on 29.08.2023
 */
@Mapper(componentModel = "spring")
interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}
