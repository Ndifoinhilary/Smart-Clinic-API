package org.bydefault.smartclinic.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Medical Report", description = "Medical Report information model")
public class MedicalReportDto {
    private Long id;

    private String clinicName;

    private String note;

    private DoctorDto doctor;

    private UserDto patient;

}
