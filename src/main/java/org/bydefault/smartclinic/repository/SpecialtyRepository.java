package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialtyRepository  extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByNameContainsIgnoreCase(String name);
}
