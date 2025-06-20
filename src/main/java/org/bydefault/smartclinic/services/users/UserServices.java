package org.bydefault.smartclinic.services.users;

import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.dtos.common.MedicalReportDto;

import java.time.LocalDateTime;
import java.util.List;

public interface UserServices {
    /**
     * Make an appointment with a doctor
     */
    String makeAppointment(Long doctorId, LocalDateTime appointmentDate, String description);

    /**
     * view a doctor's profile
     */
    DoctorDto viewDoctorProfile(Long doctorId);

    /**
     * view all doctors
     */
    List<DoctorDto> allDoctor();

    /**
     * view all past appointments of a patient
     */

    List<AppointmentDto> viewPastAppointments();

    /**
     * view the appointment details
     */
    AppointmentDto viewAppointmentDetails(Long appointmentId);

    /**
     * view a medical report for a specific appointment
     */
    MedicalReportDto viewMedicalReport(Long appointmentId);

    /**
     * view all medical reports of a patient(currently logged in)
     */
    List<MedicalReportDto> viewAllMedicalReports();

    /**
     * view all appointments of a patient(currently logged in)
     */
    List<AppointmentDto> viewAllAppointments();

    List<DoctorDto> allDoctorBySpecialty(String specialtyName);



}
