package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Appointment;
import org.bydefault.smartclinic.entities.AppointmentStatus;
import org.bydefault.smartclinic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStatus(AppointmentStatus appointmentStatus);

    List<Appointment> findByPatientAndStatus(User user, AppointmentStatus appointmentStatus);

    List<Appointment> findAllByPatient(User patient);
}
