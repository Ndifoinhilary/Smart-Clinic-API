package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository  extends JpaRepository<Specialty, Long> {
}
