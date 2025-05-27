package org.bydefault.smartclinic.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bydefault.smartclinic.entities.AppointmentStatus;

import java.io.Serializable;
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
