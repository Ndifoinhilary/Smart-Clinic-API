package org.bydefault.smartclinic.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.common.*;
import org.bydefault.smartclinic.email.EmailService;
import org.bydefault.smartclinic.entities.*;
import org.bydefault.smartclinic.exception.ResourceNotFoundException;
import org.bydefault.smartclinic.exception.UserNotVerifiedException;
import org.bydefault.smartclinic.exception.UserRoleException;
import org.bydefault.smartclinic.mappers.*;
import org.bydefault.smartclinic.repository.*;
import org.bydefault.smartclinic.services.admin.AdminServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServicesImpl implements AdminServices {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AdminUserMapper adminUserMapper;
    private final DoctorMapper doctorMapper;
    private final AppointmentMapper appointmentMapper;
    private final MedicalReportMapper medicalReportMapper;
    private final SpecialtyMapper specialtyMapper;
    private final EmailService emailService;

    @Override
    public Page<UserDto> getAllUsers(Role role, int page, int size, List<String> sortList, String sortOrder) {
        log.debug("Fetching users with role: {}, page: {}, size: {}", role, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(getSortOrders(sortList, sortOrder)));
        Page<User> userPage = userRepository.getAllUsers(role, pageable);

        log.debug("Found {} users", userPage.getTotalElements());
        return userPage.map(adminUserMapper::toDto);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);

        User user = findUserById(id);
        return adminUserMapper.toDto(user);
    }

    @Override
    @Transactional
    public DoctorDto acceptDoctor(Long id) {
        log.info("Accepting doctor application for user id: {}", id);

        User user = findUserById(id);
        validateUserForDoctorOperation(user);

        Optional<Doctor> existingDoctor = doctorRepository.findOptionalByUser(user);
        Doctor doctor;

        if (existingDoctor.isPresent()) {
            doctor = existingDoctor.get();
            log.info("Updating existing doctor application for user id: {}", id);
        } else {
            doctor = Doctor.builder()
                    .user(user)
                    .build();
            log.info("Creating new doctor record for user id: {}", id);
        }

        // Update doctor status
        doctor.setAccepted(true);
        doctor.setStatus(ApplicationStatus.APPROVED);

        // Update user role
        user.setRole(Role.DOCTOR);

        // Save both entities
        Doctor savedDoctor = doctorRepository.save(doctor);
        userRepository.save(user);

        // Send email notification asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendDoctorApplicationEmail(user.getEmail(), savedDoctor, ApplicationStatus.APPROVED);
            } catch (Exception e) {
                log.error("Failed to send doctor acceptance email for user id: {}", id, e);
            }
        });

        log.info("Doctor application accepted for user id: {}", id);
        return doctorMapper.toDto(savedDoctor);
    }

    @Override
    @Transactional
    public String rejectDoctor(Long id) {
        log.info("Rejecting doctor application for user id: {}", id);

        User user = findUserById(id);
        validateUserForDoctorOperation(user);

        Optional<Doctor> doctorOpt = doctorRepository.findOptionalByUser(user);
        if (doctorOpt.isEmpty()) {
            throw new ResourceNotFoundException("Doctor record not found for user");
        }

        Doctor doctor = doctorOpt.get();

        // Update status before deletion for email notification
        doctor.setStatus(ApplicationStatus.REJECTED);
        doctor.setAccepted(false);

        // Send rejection email asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendDoctorApplicationEmail(user.getEmail(), doctor, ApplicationStatus.REJECTED);
            } catch (Exception e) {
                log.error("Failed to send doctor rejection email for user id: {}", id, e);
            }
        });

        // Remove doctor record and reset a user role
        doctorRepository.delete(doctor);
        user.setRole(Role.PATIENT);
        userRepository.save(user);

        log.info("Doctor application rejected for user id: {}", id);
        return "Doctor rejected successfully";
    }

    @Override
    @Transactional
    public String deleteUser(Long id) {
        log.warn("Deleting user with id: {}", id);

        User user = findUserById(id);

        // Handle cascade deletion for doctor
        if (user.getRole() == Role.DOCTOR) {
            doctorRepository.findOptionalByUser(user)
                    .ifPresent(doctor -> {
                        log.debug("Deleting associated doctor record for user id: {}", id);
                        doctorRepository.delete(doctor);
                    });
        }

        userRepository.deleteById(user.getId());
        log.warn("User deleted successfully with id: {}", id);
        return "User deleted successfully";
    }

    @Override
    public List<AppointmentDto> getAllAppointments() {
        log.debug("Fetching all appointments");

        List<Appointment> appointments = appointmentRepository.findAll();
        log.debug("Found {} appointments", appointments.size());

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        log.debug("Fetching appointment with id: {}", id);

        Appointment appointment = findAppointmentById(id);
        return appointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional
    public String deleteAppointment(Long id) {
        log.warn("Deleting appointment with id: {}", id);

        Appointment appointment = findAppointmentById(id);
        appointmentRepository.delete(appointment);

        log.warn("Appointment deleted successfully with id: {}", id);
        return "Appointment deleted successfully";
    }

    @Override
    public List<AppointmentDto> getAppointmentByDoctorId(Long doctorId) {
        log.debug("Fetching appointments for doctor id: {}", doctorId);

        Doctor doctor = findDoctorById(doctorId);
        Set<Appointment> appointments = doctor.getAppointments();

        log.debug("Found {} appointments for doctor id: {}", appointments.size(), doctorId);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDto> getAppointmentByPatientId(Long patientId) {
        log.debug("Fetching appointments for patient id: {}", patientId);

        User user = findUserById(patientId);
        if (user.getRole() != Role.PATIENT) {
            throw new UserRoleException("This user is not a patient");
        }

        Set<Appointment> appointments = user.getAppointments();
        log.debug("Found {} appointments for patient id: {}", appointments.size(), patientId);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalReportDto> getReport() {
        log.debug("Fetching all medical reports");

        List<MedicalReport> medicalReports = medicalReportRepository.findAll();
        log.debug("Found {} medical reports", medicalReports.size());

        return medicalReports.stream()
                .map(medicalReportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SpecialtyDto createSpecialty(SpecialtyDto specialtyDto) {
        log.info("Creating new specialty: {}", specialtyDto.getName());

        validateSpecialtyDto(specialtyDto);

        Specialty specialty = specialtyMapper.toDomain(specialtyDto);
        Specialty savedSpecialty = specialtyRepository.save(specialty);

        log.info("Specialty created successfully with id: {}", savedSpecialty.getId());
        return specialtyMapper.toDto(savedSpecialty);
    }

    @Override
    @Transactional
    public SpecialtyDto updateSpecialty(Long id, SpecialtyDto specialtyDto) {
        log.info("Updating specialty with id: {}", id);

        validateSpecialtyDto(specialtyDto);

        Specialty specialty = findSpecialtyById(id);
        specialty.setName(specialtyDto.getName());
        specialty.setDescription(specialtyDto.getDescription());

        Specialty updatedSpecialty = specialtyRepository.save(specialty);
        log.info("Specialty updated successfully with id: {}", id);

        return specialtyMapper.toDto(updatedSpecialty);
    }

    @Override
    @Transactional
    public String deleteSpecialty(Long id) {
        log.warn("Deleting specialty with id: {}", id);

        Specialty specialty = findSpecialtyById(id);

        // Check if specialty has associated doctors
        if (!CollectionUtils.isEmpty(specialty.getDoctors())) {
            log.warn("Attempting to delete specialty with associated doctors. Specialty id: {}", id);
            throw new UserRoleException("Cannot delete specialty with associated doctors");
        }

        specialtyRepository.delete(specialty);
        log.warn("Specialty deleted successfully with id: {}", id);

        return "Specialty deleted successfully";
    }

    @Override
    public List<SpecialtyDto> getAllSpecialties() {
        log.debug("Fetching all specialties");

        List<Specialty> specialties = specialtyRepository.findAll();
        log.debug("Found {} specialties", specialties.size());

        return specialties.stream()
                .map(specialtyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> getAllDoctorsBySpecialtyId(Long specialtyId) {
        log.debug("Fetching doctors for specialty id: {}", specialtyId);

        Specialty specialty = findSpecialtyById(specialtyId);
        Set<Doctor> doctors = specialty.getDoctors();

        log.debug("Found {} doctors for specialty id: {}", doctors.size(), specialtyId);
        return doctors.stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    private Specialty findSpecialtyById(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id));
    }

    private void validateUserForDoctorOperation(User user) {
        if (!user.getIsVerified()) {
            throw new UserNotVerifiedException("User is not yet verified");
        }
    }

    private void validateSpecialtyDto(SpecialtyDto specialtyDto) {
        if (specialtyDto == null) {
            throw new IllegalArgumentException("Specialty data cannot be null");
        }
        if (specialtyDto.getName() == null || specialtyDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Specialty name cannot be null or empty");
        }
    }

    private List<Sort.Order> getSortOrders(List<String> sortList, String sortOrder) {
        if (CollectionUtils.isEmpty(sortList)) {
            return List.of(Sort.Order.desc("id"));
        }

        return sortList.stream()
                .map(sort -> "asc".equalsIgnoreCase(sortOrder)
                        ? Sort.Order.asc(sort)
                        : Sort.Order.desc(sort))
                .collect(Collectors.toList());
    }
}
