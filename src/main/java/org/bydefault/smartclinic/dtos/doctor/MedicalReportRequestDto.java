package org.bydefault.smartclinic.dtos.doctor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicalReportRequestDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String clinicName;

    private String reportType;

    private String note;

    private LocalDate reportDate;

}
