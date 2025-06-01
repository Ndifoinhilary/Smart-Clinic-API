package org.bydefault.smartclinic.dtos.doctor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorApplicationDto {
    @NotBlank(message = "Name is required")
    private String location;

    @NotBlank(message = "Give your highest qualification")
    private String highersQualifications;

    @NotBlank(message = "Id photo is required")
    private String idPhoto;

    @NotBlank(message = "Certificate is required")
    private String certificate;

    private String anyOtherQualifications;

    @NotBlank(message = "Video is required")
    private String shortVideo;

}
