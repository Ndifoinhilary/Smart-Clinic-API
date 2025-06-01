package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.entities.Doctor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    DoctorDto toDto(Doctor doctor);
}
