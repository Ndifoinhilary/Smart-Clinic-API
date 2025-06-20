package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.MedicalReport;
import org.bydefault.smartclinic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalReportRepository  extends JpaRepository<MedicalReport, Long> {
    List<MedicalReport> findByPatient(User patient);
}
