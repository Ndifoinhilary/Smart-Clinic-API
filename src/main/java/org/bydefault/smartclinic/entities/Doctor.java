package org.bydefault.smartclinic.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;

    private String highersQualifications;

    private String idPhoto;

    private String certificate;

    private String anyOtherQualifications;

    private String shortVideo;

    private boolean accepted;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Availability> availabilities = new HashSet<>();

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Updated patient relationship - better to use Set for unique patients
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.PERSIST)
    private Set<User> patients = new HashSet<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Appointment> appointments = new HashSet<>();

    // =======================
    // BUSINESS LOGIC METHODS
    // =======================

    /*
     * Get a doctor's full name from an associated auth
     */
    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    /*
     * Get doctors specialty name
     */
    public String getSpecialtyName() {
        return specialty != null ? specialty.getName() : "General Practice";
    }

    /*
     * Get available time slots for a specific date
     */
    public List<Availability> getAvailableTimeSlots(LocalDate date) {
        return availabilities.stream()
                .filter(availability ->
                        availability.getDate().equals(date) &&
                                availability.isAvailable())
                .collect(Collectors.toList());
    }

    /*
     * Get all appointments for a specific date
     */
    public List<Appointment> getAppointments(LocalDate date) {
        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Get past appointments
     */
    public List<Appointment> getPastAppointments() {
        LocalDateTime now = LocalDateTime.now();
        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().isBefore(now))
                .sorted((a1, a2) -> a2.getAppointmentDate().compareTo(a1.getAppointmentDate()))
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming appointments
     */
    public List<Appointment> getUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().isAfter(now))
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.ACCEPTED) ||
                        appointment.getStatus().equals(AppointmentStatus.PENDING))
                .sorted((a1, a2) -> a1.getAppointmentDate().compareTo(a2.getAppointmentDate()))
                .collect(Collectors.toList());
    }

    /**
     * Count total appointments
     */
    public int getTotalAppointments() {
        return this.appointments.size();
    }

    /**
     * Count total patients
     */
    public int getTotalPatients() {
        return this.patients.size();
    }

    /**
     * Count appointments for today
     */
    public long getTodayAppointmentsCount() {
        LocalDate today = LocalDate.now();
        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().toLocalDate().equals(today))
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.ACCEPTED) ||
                        appointment.getStatus().equals(AppointmentStatus.PENDING))
                .count();
    }

    /**
     * Check if the doctor has any appointments today
     */
    public boolean hasAppointmentsToday() {
        return getTodayAppointmentsCount() > 0;
    }

    /**
     * Get pending appointments that need approval
     */
    public List<Appointment> getPendingAppointments() {
        return appointments.stream()
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.PENDING))
                .sorted((a1, a2) -> a1.getAppointmentDate().compareTo(a2.getAppointmentDate()))
                .collect(Collectors.toList());
    }

    /**
     * Check if a user is already a patient of this doctor
     */
    public boolean isPatient(User user) {
        return patients.contains(user);
    }

    // =======================
    // HELPER METHODS FOR RELATIONSHIPS
    // =======================

    /**
     * Add availability slot
     */
    public void addAvailability(Availability availability) {
        availabilities.add(availability);
        availability.setDoctor(this);
    }

    /**
     * Remove availability slot
     */
    public void removeAvailability(Availability availability) {
        availabilities.remove(availability);
        availability.setDoctor(null);
    }

    /**
     * Add appointment
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setDoctor(this);
    }

    /**
     * Remove appointment
     */
    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setDoctor(null);
    }

    /**
     * Add patient to doctor's patient list
     */
    public void addPatient(User patient) {
        if (patient.getRole().equals(Role.PATIENT)) {
            patients.add(patient);
            patient.setDoctor(this);
        }
    }

    /**
     * Remove patient from doctor's patient list
     */
    public void removePatient(User patient) {
        patients.remove(patient);
        patient.setDoctor(null);
    }

    /**
     * Get all patients as a list
     */
    public List<User> getPatientsList() {
        return patients.stream()
                .sorted((p1, p2) -> p1.getFirstName().compareTo(p2.getFirstName()))
                .collect(Collectors.toList());
    }
}
