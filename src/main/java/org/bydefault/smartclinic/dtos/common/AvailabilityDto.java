package org.bydefault.smartclinic.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.bydefault.smartclinic.entities.Day;

import java.util.Date;

@Data
@Schema(name = "Availability", description = "Availability information model")
public class AvailabilityDto {
    private Long id;

    private Day day;

    private String time;

    private Date date;

    private boolean isAvailable;
}
