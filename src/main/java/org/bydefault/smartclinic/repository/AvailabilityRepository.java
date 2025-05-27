package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
