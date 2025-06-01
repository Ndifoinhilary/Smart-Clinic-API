package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.common.SpecialtyDto;
import org.bydefault.smartclinic.entities.Specialty;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {
    SpecialtyDto toDto(Specialty specialty);
    Specialty toDomain(SpecialtyDto specialtyDto);
}
