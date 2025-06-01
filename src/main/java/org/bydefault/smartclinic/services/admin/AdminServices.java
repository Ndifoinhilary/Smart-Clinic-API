package org.bydefault.smartclinic.services.admin;

import org.bydefault.smartclinic.dtos.common.*;
import org.bydefault.smartclinic.entities.Role;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminServices {

    Page<UserDto> getAllUsers(Role role, int page, int size, List<String> sortList, String sortOrder);

    UserDto getUserById(Long id);

    DoctorDto acceptDoctor(Long id);

    String rejectDoctor(Long id);

    String deleteUser(Long id);

    List<AppointmentDto> getAllAppointments();

    AppointmentDto getAppointmentById(Long id);

    String deleteAppointment(Long id);

    List<AppointmentDto> getAppointmentByDoctorId(Long doctorId);

    List<AppointmentDto> getAppointmentByPatientId(Long id);

    List<MedicalReportDto> getReport();

    SpecialtyDto createSpecialty(SpecialtyDto specialtyDto);

    SpecialtyDto updateSpecialty(Long id,  SpecialtyDto specialtyDto);

    String deleteSpecialty(Long id);

    List<SpecialtyDto> getAllSpecialties();

    List<DoctorDto> getAllDoctorsBySpecialtyId(Long specialtyId);
}
