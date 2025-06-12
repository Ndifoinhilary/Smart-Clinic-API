package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Availability;
import org.bydefault.smartclinic.entities.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDayAndTime(Day dayEnum, String time);
}
