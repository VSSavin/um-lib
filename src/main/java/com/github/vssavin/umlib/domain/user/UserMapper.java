package com.github.vssavin.umlib.domain.user;

import org.mapstruct.Mapper;

/**
 * Mapper to convert a user entity to a corresponding data transfer object and vice versa.
 *
 * @author vssavin on 29.08.2023
 */
@Mapper(componentModel = "spring")
interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}
