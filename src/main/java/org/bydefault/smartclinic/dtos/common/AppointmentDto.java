package org.bydefault.smartclinic.dtos.common;

import lombok.Data;
import org.bydefault.smartclinic.entities.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link org.bydefault.smartclinic.entities.Appointment}
 */
@Data
public class AppointmentDto{
    private Long id;
    private String description;
    private Date date;
    private String time;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
