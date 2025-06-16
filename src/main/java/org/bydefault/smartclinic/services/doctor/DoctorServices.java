package org.bydefault.smartclinic.services.doctor;

import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.dtos.common.AvailabilityDto;
import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.dtos.common.MedicalReportDto;
import org.bydefault.smartclinic.dtos.doctor.DoctorApplicationDto;
import org.bydefault.smartclinic.dtos.doctor.MedicalReportRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface DoctorServices {
    DoctorApplicationDto submitApplication(
            DoctorApplicationDto applicationDto,
            MultipartFile idPhoto,
            MultipartFile certificate,
            MultipartFile shortVideo
    );

    AvailabilityDto addAvailability(
            AvailabilityDto availabilityDto
    );

    AvailabilityDto updateAvailability(
            Long availabilityId,
            AvailabilityDto availabilityDto
    );

    String deleteAvailability(
            Long availabilityId
    );

    AvailabilityDto getAvailabilityById(
            Long availabilityId
    );

    List<AvailabilityDto> getAvailabilityByDoctor(Long doctorId);

    List<DoctorDto> getAllDoctorsByAvailability(
            String day,
            LocalTime time
    );

    List<DoctorDto> getAllDoctorsBySpecialty(
            String specialtyName
    );

    List<DoctorDto> getAllDoctorsByLocation(
            String location
    );

    List<DoctorDto> getAllDoctorsByDate(LocalDate date);

    MedicalReportDto createMedicalReport(
            MedicalReportRequestDto medicalReportRequestDto,
            Long userId
    );

    String acceptAppointment(
            Long appointmentId
    );

    String  cancelAppointment(Long appointmentId);

    List<AppointmentDto> allAcceptedAppointments();

    List<AppointmentDto> allRejectedAppointments();

    String rescheduleAppointment(Long appointmentId, LocalDateTime newAppointmentDate);



}
