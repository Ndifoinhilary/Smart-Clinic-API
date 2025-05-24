package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.user.UserDto;
import org.bydefault.smartclinic.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
