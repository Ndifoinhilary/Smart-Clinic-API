package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalReportRepository  extends JpaRepository<MedicalReport, Long> {
}
