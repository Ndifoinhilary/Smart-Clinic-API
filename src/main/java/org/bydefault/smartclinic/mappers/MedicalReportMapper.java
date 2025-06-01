package org.bydefault.smartclinic.mappers;

import org.bydefault.smartclinic.dtos.common.MedicalReportDto;
import org.bydefault.smartclinic.entities.MedicalReport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicalReportMapper {
    MedicalReportDto toDto(MedicalReport medicalReport);
}
