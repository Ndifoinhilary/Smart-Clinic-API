package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.common.ProfileDto;
import org.bydefault.smartclinic.entities.Profile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileDto toDto(Profile profile);
    Profile toEntity(ProfileDto profileDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProfileDto profileDto, @MappingTarget Profile profile);
}
