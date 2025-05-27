package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {
}
