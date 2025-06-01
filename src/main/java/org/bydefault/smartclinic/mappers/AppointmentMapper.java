package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.entities.Appointment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentDto toDto(Appointment appointment);
}
