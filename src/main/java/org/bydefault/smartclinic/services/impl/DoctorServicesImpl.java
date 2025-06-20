package org.bydefault.smartclinic.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.dtos.common.AvailabilityDto;
import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.dtos.common.MedicalReportDto;
import org.bydefault.smartclinic.dtos.doctor.DoctorApplicationDto;
import org.bydefault.smartclinic.dtos.doctor.MedicalReportRequestDto;
import org.bydefault.smartclinic.email.EmailService;
import org.bydefault.smartclinic.entities.*;
import org.bydefault.smartclinic.exception.InvalidStateException;
import org.bydefault.smartclinic.exception.ResourceNotFoundException;
import org.bydefault.smartclinic.exception.UnAuthorizedException;
import org.bydefault.smartclinic.exception.UserAlreadyExistException;
import org.bydefault.smartclinic.mappers.AppointmentMapper;
import org.bydefault.smartclinic.mappers.AvailabilityMapper;
import org.bydefault.smartclinic.mappers.DoctorMapper;
import org.bydefault.smartclinic.mappers.MedicalReportMapper;
import org.bydefault.smartclinic.repository.*;
import org.bydefault.smartclinic.services.ImageService;
import org.bydefault.smartclinic.services.doctor.DoctorServices;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j

public class DoctorServicesImpl implements DoctorServices {
    private final AppointmentMapper appointmentMapper;

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final AvailabilityMapper availabilityMapper;
    private final DoctorMapper doctorMapper;
    private final MedicalReportMapper medicalReportMapper;
    private final MedicalReportRepository medicalReportRepository;

    @Override
    @Transactional
    public DoctorApplicationDto submitApplication(DoctorApplicationDto applicationDto, MultipartFile idPhoto, MultipartFile certificate, MultipartFile shortVideo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (doctorRepository.existsByUserId(user.getId())) {
            throw new UserAlreadyExistException("You have already submitted a doctor application");
        }
        String specialty_name = applicationDto.getSpecialty().getName().toLowerCase();
        Specialty specialty = specialtyRepository.findByNameContainsIgnoreCase(specialty_name)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + specialty_name));

        try {
            // Upload files
            String idPhotoUrl = imageService.storeFile(idPhoto, "doctor-applications/id-photos");
            String certificateUrl = imageService.storeFile(certificate, "doctor-applications/certificates");
            String videoUrl = imageService.storeFile(shortVideo, "doctor-applications/videos");

            // Create an application entity
            Doctor application = new Doctor();
            application.setUser(user);
            application.setLocation(applicationDto.getLocation());
            application.setHighersQualifications(applicationDto.getHighersQualifications());
            application.setAnyOtherQualifications(applicationDto.getAnyOtherQualifications());
            application.setIdPhoto(idPhotoUrl);
            application.setCertificate(certificateUrl);
            application.setShortVideo(videoUrl);
            application.setSpecialty(specialty);
            application.setStatus(ApplicationStatus.PENDING);
            application.setSubmittedAt(LocalDateTime.now());
            application.setAccepted(false);


            var savedDoctor = doctorRepository.save(application);
            emailService.sendDoctorApplicationEmail(user.getEmail(), savedDoctor, ApplicationStatus.PENDING);

            // Return DTO
            applicationDto.setIdPhoto(idPhotoUrl);
            applicationDto.setCertificate(certificateUrl);
            applicationDto.setShortVideo(videoUrl);

            return applicationDto;

        } catch (Exception e) {
            log.error("Error storing files: {}", e.getMessage());
            throw new RuntimeException("Failed to process application files", e);
        }
    }

    @Override
    public AvailabilityDto addAvailability(AvailabilityDto availabilityDto) {
        // Get the currently authenticated user
        User user = getDoctor();
        // Get the doctor's ID from the user

        Doctor doctor = doctorRepository.findByUser(user);

        // Create a new Availability entity from the DTO
        Availability availability = new Availability();
        availability.setDay(availabilityDto.getDay());
        availability.setTime(availabilityDto.getTime());
        availability.setDate(availabilityDto.getDate());
        availability.setAvailable(availabilityDto.isAvailable());

        // Use the helper method to properly manage the bidirectional relationship
        doctor.addAvailability(availability);

        // Save the doctor (which will cascade to save the availability)
        doctorRepository.save(doctor);

        // Return the DTO with the generated ID
        availabilityDto.setId(availability.getId());
        return availabilityDto;
    }

    @Override
    public AvailabilityDto updateAvailability(Long availabilityId, AvailabilityDto availabilityDto) {
        User user = getDoctor();
        // get the availability by ID and check if it belongs to the doctor
        Availability availability = availabilityRepository.findById(availabilityId).orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));
        Doctor doctor = doctorRepository.findByUser(user);
        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new ResourceNotFoundException("Availability does not belong to the doctor");
        }
        // Update the availability fields
        availability.setDay(availabilityDto.getDay());
        availability.setTime(availabilityDto.getTime());
        availability.setDate(availabilityDto.getDate());
        availability.setAvailable(availabilityDto.isAvailable());
        // Save the updated availability
        availabilityRepository.save(availability);
        // Return the updated DTO
        availabilityDto.setId(availability.getId());
        return availabilityDto;
    }


    @Override
    public String deleteAvailability(Long availabilityId) {
//        get the currently authenticated user that's a doctor
        User user = getDoctor();

        Doctor doctor = doctorRepository.findByUser(user);

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new ResourceNotFoundException("Availability does not belong to the doctor");
        }
        // Delete the availability
        availabilityRepository.delete(availability);

        return " Availability with ID " + availabilityId + " deleted successfully.";
    }

    @Override
    public AvailabilityDto getAvailabilityById(Long availabilityId) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));
        // Convert the Availability entity to AvailabilityDto
        AvailabilityDto availabilityDto = new AvailabilityDto();
        availabilityDto.setId(availability.getId());
        availabilityDto.setDay(availability.getDay());
        availabilityDto.setTime(availability.getTime());
        availabilityDto.setDate(availability.getDate());
        availabilityDto.setAvailable(availability.isAvailable());
        return availabilityDto;
    }

    @Override
    public List<AvailabilityDto> getAvailabilityByDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        // Get all availabilities for the doctor
        Set<Availability> availabilities = doctor.getAvailabilities();
        return availabilities.stream().map(availabilityMapper::toDto).toList();
    }

    @Override
    public List<DoctorDto> getAllDoctorsByAvailability(String day, LocalTime time) {
        // This method should return a list of doctors available on a specific day and time
        // Convert the day string to a Day enum
        Day dayEnum;
        try {
            dayEnum = Day.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid day: " + day);
        }
        // Find all availabilities for the specified day and time
        List<Availability> availabilities = availabilityRepository.findByDayAndTime(dayEnum, String.valueOf(time));
        if (availabilities.isEmpty()) {
            throw new ResourceNotFoundException("No doctors available on " + day + " at " + time);
        }
        // Map the availabilities to DoctorDto
        return availabilities.stream()
                .map(Availability::getDoctor)
                .distinct()
                .map(doctorMapper::toDto)
                .toList();
    }

    @Override
    public List<DoctorDto> getAllDoctorsBySpecialty(String specialtyName) {
        // This method should return a list of doctors by specialty
        Specialty specialty = specialtyRepository.findByNameContainsIgnoreCase(specialtyName)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + specialtyName));

        List<Doctor> doctors = doctorRepository.findBySpecialty(specialty);
        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException("No doctors found for specialty: " + specialtyName);
        }
        return doctors.stream().map(doctorMapper::toDto).toList();
    }

    @Override
    public List<DoctorDto> getAllDoctorsByLocation(String location) {
        // This method should return a list of doctors by location
        List<Doctor> doctors = doctorRepository.findByLocationContainsIgnoreCase(location);
        if (!doctors.isEmpty()) {
            return doctors.stream().map(doctorMapper::toDto).toList();
        }
        throw new ResourceNotFoundException("No doctors found in location: " + location);
    }

    @Override
    public List<DoctorDto> getAllDoctorsByDate(LocalDate date) {
        // This method should return a list of doctors available on a specific date
        List<Doctor> doctors = doctorRepository.findAll();
        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException("No doctors found");
        }
        return doctors.stream()
                .filter(doctor -> !doctor.getAvailableTimeSlots(date).isEmpty())
                .map(doctorMapper::toDto)
                .toList();
    }

    @Override
    public MedicalReportDto createMedicalReport(MedicalReportRequestDto medicalReportDto, Long userId) {
        // This method should create a medical report for a user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        // Check if the user is a patient and has a doctor assigned
        // check if the logged-in user is a doctor
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long doctorId = (Long) authentication.getPrincipal();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (user.getRole() != Role.PATIENT || user.getDoctor() == null || !user.getDoctor().getId().equals(doctor.getId())) {
            throw new ResourceNotFoundException("User is not a patient or does not have a doctor assigned");
        }
        // Create a new MedicalReport entity from the DTO
        MedicalReport medicalReport = medicalReportMapper.toEntity(medicalReportDto);
        medicalReport.setDoctor(doctor);
        medicalReport.setPatient(user);
        medicalReportRepository.save(medicalReport);
        // Convert the MedicalReport entity to MedicalReportDto
        MedicalReportDto reportDto = medicalReportMapper.toDto(medicalReport);
        reportDto.setId(medicalReport.getId());
        return reportDto;
    }

    @Override
    @Transactional
    public String acceptAppointment(Long appointmentId) {
        // Find the appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Check if the appointment is already completed or canceled
        if (appointment.getStatus().equals(AppointmentStatus.COMPLETED) ||
                appointment.getStatus().equals(AppointmentStatus.CANCELED)) {
            throw new InvalidStateException("Cannot accept appointment with status: " + appointment.getStatus());
        }

        // Update the appointment status to ACCEPTED
        appointment.setStatus(AppointmentStatus.ACCEPTED);

        // Get the patient and doctor from the appointment
        User patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();

        // Add the patient to the doctor's patient list if not already added
        if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            patient.setDoctor(doctor);
            userRepository.save(patient);
        }


        appointmentRepository.save(appointment);
        // Send an email notification to the patient

        return "Appointment with ID " + appointmentId + " accepted successfully. Patient " +
                patient.getFullName() + " is now registered under Dr. " + doctor.getFullName();
    }

    @Override
    @Transactional
    public String cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        // Check if the user is either the patient or the doctor
        boolean isPatient = appointment.getPatient().getId().equals(userId);
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(userId);

        if (!isPatient && !isDoctor) {
            throw new UnAuthorizedException("You can only cancel your own appointments");
        }

        // Check if appointment can be canceled
        if (appointment.getStatus().equals(AppointmentStatus.COMPLETED) ||
                appointment.getStatus().equals(AppointmentStatus.CANCELED)) {
            throw new InvalidStateException("Cannot cancel appointment with status: " + appointment.getStatus());
        }

        // Update status to canceled
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);

        return "Appointment with ID " + appointmentId + " has been canceled successfully.";
    }

    @Override
    public List<AppointmentDto> allAcceptedAppointments() {
        // This method should return a list of all accepted appointments
        List<Appointment> acceptedAppointments = appointmentRepository.findByStatus(AppointmentStatus.ACCEPTED);
        if (!acceptedAppointments.isEmpty()) {
            return acceptedAppointments.stream().map(appointmentMapper::toDto).toList();
        }
        throw new ResourceNotFoundException("No accepted appointments found");
    }

    /**
     * Reschedule an appointment
     */
    @Override
    @Transactional
    public String rescheduleAppointment(Long appointmentId, LocalDateTime newAppointmentDate) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();

        // Check if the user is either the patient or the doctor
        boolean isPatient = appointment.getPatient().getId().equals(userId);
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(userId);

        if (!isPatient && !isDoctor) {
            throw new UnAuthorizedException("You can only reschedule your own appointments");
        }

        // Check if appointment can be rescheduled
        if (appointment.getStatus().equals(AppointmentStatus.COMPLETED) ||
                appointment.getStatus().equals(AppointmentStatus.CANCELED)) {
            throw new InvalidStateException("Cannot reschedule appointment with status: " + appointment.getStatus());
        }

        // Validate new date is in the future
        if (newAppointmentDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New appointment date must be in the future");
        }

        // Check for conflicts at the new time
        boolean hasConflict = appointment.getDoctor().getAppointments().stream()
                .filter(apt -> !apt.getId().equals(appointmentId))
                .anyMatch(existingAppointment ->
                        existingAppointment.getAppointmentDate().equals(newAppointmentDate) &&
                                (existingAppointment.getStatus().equals(AppointmentStatus.PENDING) ||
                                        existingAppointment.getStatus().equals(AppointmentStatus.ACCEPTED)));

        if (hasConflict) {
            throw new InvalidStateException("Doctor already has an appointment at the requested time");
        }

        // Update the appointment
        appointment.setAppointmentDate(newAppointmentDate);
        appointment.setDate(java.sql.Date.valueOf(newAppointmentDate.toLocalDate()));
        appointment.setTime(newAppointmentDate.toLocalTime().toString());

        // If it was accepted, set it back to pending for doctor approval
        if (appointment.getStatus().equals(AppointmentStatus.ACCEPTED)) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        appointmentRepository.save(appointment);

        return String.format("Appointment with ID %d has been rescheduled to %s. Status: %s",
                appointmentId,
                newAppointmentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                appointment.getStatus());
    }

    @Override
    public List<AppointmentDto> allRejectedAppointments() {
        // This method should return a list of all rejected appointments
        List<Appointment> rejectedAppointments = appointmentRepository.findByStatus(AppointmentStatus.REJECTED);
        if (rejectedAppointments.isEmpty()) {
            throw new ResourceNotFoundException("No rejected appointments found");
        }
        return rejectedAppointments.stream().map(appointmentMapper::toDto).toList();
    }

    private User getDoctor() {
        //this method retrieves the currently authenticated user and checks if they are a doctor
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        // Check if the user is a doctor
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() != Role.DOCTOR) {
            throw new ResourceNotFoundException("User is not a doctor");
        }
        return user;
    }

}
