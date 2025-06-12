package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.common.AvailabilityDto;
import org.bydefault.smartclinic.entities.Availability;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    AvailabilityDto toDto(Availability availability);
}
