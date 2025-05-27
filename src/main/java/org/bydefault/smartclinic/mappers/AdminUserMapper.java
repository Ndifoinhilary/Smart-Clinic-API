package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.admin.UserDto;
import org.bydefault.smartclinic.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    UserDto toDto(User user);
}
