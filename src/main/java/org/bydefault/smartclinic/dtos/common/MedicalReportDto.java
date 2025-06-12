package org.bydefault.smartclinic.dtos.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Medical Report", description = "Medical Report information model")
public class MedicalReportDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String clinicName;

    private String note;

    private DoctorDto doctor;

    private UserDto patient;

}
