package org.bydefault.smartclinic.dtos.doctor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorApplicationDto {
    @NotBlank(message = "Name is required")
    private String location;

    @NotBlank(message = "Give your highest qualification")
    private String highersQualifications;


    private String idPhoto;


    private String certificate;

    private String anyOtherQualifications;


    private String shortVideo;

    private SpecialtyDto specialty;
}
