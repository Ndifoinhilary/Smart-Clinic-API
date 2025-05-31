package org.bydefault.smartclinic.dtos.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.bydefault.smartclinic.entities.Availability;
import org.bydefault.smartclinic.entities.Specialty;
import org.bydefault.smartclinic.entities.User;

import java.util.Set;

@Data
@Schema(name = "Doctor", description = "Doctor information model")
public class DoctorDto {
    private Long id;

    private String location;

    private String highersQualifications;

    private String idPhoto;

    private String certificate;

    private String anyOtherQualifications;

    private boolean accepted;

    private Specialty specialty;

    private Set<AvailabilityDto> availabilities;

    private ProfileDto profile;

}
