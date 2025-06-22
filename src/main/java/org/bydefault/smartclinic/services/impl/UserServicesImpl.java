package org.bydefault.smartclinic.services.impl;

import lombok.RequiredArgsConstructor;
import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.dtos.common.MedicalReportDto;
import org.bydefault.smartclinic.email.EmailService;
import org.bydefault.smartclinic.entities.*;
import org.bydefault.smartclinic.exception.DoctorNotAcceptedException;
import org.bydefault.smartclinic.exception.ResourceNotFoundException;
import org.bydefault.smartclinic.exception.UnAuthorizedException;
import org.bydefault.smartclinic.mappers.AppointmentMapper;
import org.bydefault.smartclinic.mappers.DoctorMapper;
import org.bydefault.smartclinic.mappers.MedicalReportMapper;
import org.bydefault.smartclinic.repository.AppointmentRepository;
import org.bydefault.smartclinic.repository.DoctorRepository;
import org.bydefault.smartclinic.repository.MedicalReportRepository;
import org.bydefault.smartclinic.repository.UserRepository;
import org.bydefault.smartclinic.services.users.UserServices;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServicesImpl implements UserServices {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final DoctorMapper doctorMapper;
    private final AppointmentMapper appointmentMapper;
    private final MedicalReportMapper medicalReportMapper;

    @Override
    public String makeAppointment(Long doctorId, LocalDateTime appointmentDate, String description) {
        // Logic to make an appointment

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Only patients can make appointments.");
        }

        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        if (!doctor.isAccepted()) {
            throw new DoctorNotAcceptedException("Doctor with id: " + doctorId + " has not been accepted yet.");
        }
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }
        LocalDate requestedDate = appointmentDate.toLocalDate();
        List<Availability> availabilities = doctor.getAvailableTimeSlots(requestedDate);
        if (availabilities.isEmpty()) {
            throw new ResourceNotFoundException("No available time slots for doctor with id: " + doctorId + " on date: " + requestedDate);
        }
        boolean hasConflict = doctor.getAppointments().stream()
                .anyMatch(existingAppointment ->
                        existingAppointment.getAppointmentDate().equals(appointmentDate) &&
                                (existingAppointment.getStatus().equals(AppointmentStatus.PENDING) ||
                                        existingAppointment.getStatus().equals(AppointmentStatus.ACCEPTED)));

        if (hasConflict) {
            throw new IllegalStateException("Doctor already has an appointment at the requested time");
        }
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setDate(java.sql.Date.valueOf(requestedDate));
        appointment.setTime(appointmentDate.toLocalTime().toString());
        appointment.setDescription(description != null ? description : "");
        appointmentRepository.save(appointment);

        return "Appointment scheduled successfully with Dr. " + doctor.getFullName() +
                " for " + appointmentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                ". Appointment ID: " + appointment.getId() + ". Status: PENDING";
    }

    @Override
    public DoctorDto viewDoctorProfile(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        return doctorMapper.toDto(doctor);
    }

    @Override
    public List<DoctorDto> allDoctor() {
        List<Doctor> doctors = doctorRepository.findAllByAccepted(true);
        return doctors.stream().map(doctorMapper::toDto).toList();
    }

    @Override
    public List<AppointmentDto> viewPastAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Only patients can view past appointments.");
        }
        List<Appointment> pastAppointments = appointmentRepository.findByPatientAndStatus(user, AppointmentStatus.COMPLETED);
        return pastAppointments.stream().map(appointmentMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public AppointmentDto viewAppointmentDetails(Long appointmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        if (appointment.getPatient() == null) {
            throw new ResourceNotFoundException("Appointment with id: " + appointmentId + " has no associated patient.");
        }
        if (!appointment.getPatient().getId().equals(userId)) {
            throw new UnAuthorizedException("You do not have permission to view this appointment.");
        }
        return appointmentMapper.toDto(appointment);
    }

    @Override
    public MedicalReportDto viewMedicalReport(Long reportId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        MedicalReport medicalReport = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical report not found with id: " + reportId));


        if (!medicalReport.getPatient().getId().equals(userId)) {
            throw new UnAuthorizedException("You do not have permission to view this medical report.");
        }

        return medicalReportMapper.toDto(medicalReport);
    }

    @Override
    public List<MedicalReportDto> viewAllMedicalReports() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Only patients can view medical reports.");
        }
        List<MedicalReport> medicalReports = medicalReportRepository.findByPatient(user);
        if (medicalReports.isEmpty()) {
            throw new ResourceNotFoundException("No medical reports found for user with id: " + userId);
        }
        return medicalReports.stream().map(medicalReportMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDto> viewAllAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Only patients can view their appointments.");
        }
        List<Appointment> appointments = appointmentRepository.findAllByPatient(user);
        if (appointments.isEmpty()) {
            throw new ResourceNotFoundException("No appointments found for user with id: " + userId);
        }
        return appointments.stream().map(appointmentMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> allDoctorBySpecialty(String specialtyName) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyName(specialtyName);
        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException("No doctors found with specialty: " + specialtyName);
        }
        return doctors.stream().map(doctorMapper::toDto).collect(Collectors.toList());
    }

}
