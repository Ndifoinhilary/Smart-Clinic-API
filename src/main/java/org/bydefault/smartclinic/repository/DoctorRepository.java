package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Doctor;
import org.bydefault.smartclinic.entities.Specialty;
import org.bydefault.smartclinic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUser(User user);

    Optional<Doctor> findOptionalByUser(User user);

    boolean existsByUserId(Long userId);

    List<Doctor> findBySpecialty(Specialty specialty);

    List<Doctor> findByLocationContainsIgnoreCase(String location);

    List<Doctor> findAllByAccepted(boolean b);

    @Query("SELECT d FROM Doctor d JOIN d.specialty s WHERE s.name = LOWER(:specialtyName) ")
    List<Doctor> findBySpecialtyName(String specialtyName);
}
