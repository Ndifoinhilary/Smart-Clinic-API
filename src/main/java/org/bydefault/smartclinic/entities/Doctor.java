package org.bydefault.smartclinic.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;

    private String highersQualifications;

    private String idPhoto;

    private String certificate;

    private String anyOtherQualifications;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Availability> availabilities;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToMany(mappedBy = "doctor")
    private Set<Appointment> appointments;


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
                                availability.isAvailable()).toList();
    }

    /*
     * Get all appointments for a specific date
     */

    public List<Appointment> getAppointments(LocalDate date) {
        return appointments.stream()
                .filter(appointment -> appointment.getDate().equals(date))
                .toList();
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
     * Count total appointments
     */
    public int getTotalAppointments() {
        return  this.appointments.size();
    }

    /**
     * Count appointments for today
     */
    public long getTodayAppointmentsCount() {
        LocalDate today = LocalDate.now();
        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().toLocalDate().equals(today))
                .count();
    }

    /**
     * Check if the doctor has any appointments today
     */
    public boolean hasAppointmentsToday() {
        return getTodayAppointmentsCount() > 0;
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


}
