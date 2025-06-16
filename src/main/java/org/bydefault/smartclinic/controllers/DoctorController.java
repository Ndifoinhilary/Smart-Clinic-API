package org.bydefault.smartclinic.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.dtos.common.AvailabilityDto;
import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.dtos.common.MedicalReportDto;
import org.bydefault.smartclinic.dtos.doctor.DoctorApplicationDto;
import org.bydefault.smartclinic.dtos.doctor.MedicalReportRequestDto;
import org.bydefault.smartclinic.services.ImageService;
import org.bydefault.smartclinic.services.doctor.DoctorServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor")
@Slf4j
public class DoctorController {
    private final DoctorServices doctorServices;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ImageService imageService;


    @PostMapping(value = "/apply/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DoctorApplicationDto> applyToBeDoctor(
            @RequestPart("application") String applicationJson,
            @RequestPart("idPhoto") MultipartFile idPhoto,
            @RequestPart("certificate") MultipartFile certificate,
            @RequestPart("shortVideo") MultipartFile shortVideo
    ) {

        try {
            // Parse JSON
            DoctorApplicationDto applicationDto = objectMapper.readValue(applicationJson, DoctorApplicationDto.class);

            // Validate DTO
            Set<ConstraintViolation<DoctorApplicationDto>> violations = validator.validate(applicationDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            imageService.validateFiles(idPhoto, certificate, shortVideo);

            return ResponseEntity.ok(doctorServices.submitApplication(applicationDto, idPhoto, certificate, shortVideo));

        } catch (JsonProcessingException e) {
            log.error("Error parsing application JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ConstraintViolationException e) {
            log.error("Validation errors: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing doctor application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/availability/")
    public ResponseEntity<AvailabilityDto> addAvailability(
         @Valid @RequestBody AvailabilityDto availabilityDto
    ) {
        return ResponseEntity.ok(doctorServices.addAvailability(availabilityDto));
    }

    @PutMapping("/availability/{availabilityId}/")
    public ResponseEntity<AvailabilityDto> updateAvailability(@PathVariable Long availabilityId,
                                                              @Valid @RequestBody AvailabilityDto availabilityDto) {
        return ResponseEntity.ok(doctorServices.updateAvailability(availabilityId, availabilityDto));
    }

    @DeleteMapping("/availability/{availabilityId}/")
    public ResponseEntity<String> deleteAvailability(@PathVariable Long availabilityId) {
        String response = doctorServices.deleteAvailability(availabilityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/availability/{availabilityId}/")
    public ResponseEntity<AvailabilityDto> getAvailabilityById(@PathVariable Long availabilityId) {
        AvailabilityDto availabilityDto = doctorServices.getAvailabilityById(availabilityId);
        return ResponseEntity.ok(availabilityDto);
    }

    @GetMapping("/availability/doctor/{doctorId}/")
    public ResponseEntity<List<AvailabilityDto>> getAvailabilityByDoctor(@PathVariable Long doctorId) {
        List<AvailabilityDto> availabilityDtos = doctorServices.getAvailabilityByDoctor(doctorId);
        return ResponseEntity.ok(availabilityDtos);
    }

    @GetMapping("/availability/doctor/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsByAvailability(@RequestParam String day, @RequestParam String time) {
        LocalTime Localtime = LocalTime.parse(time);
        List<DoctorDto> availabilityDtos = doctorServices.getAllDoctorsByAvailability(day, Localtime);
        return ResponseEntity.ok(availabilityDtos);
    }

    @GetMapping("/specialty/doctors/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsBySpecialty(@RequestParam String specialtyName) {
        List<DoctorDto> doctors = doctorServices.getAllDoctorsBySpecialty(specialtyName);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/location/doctors/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsByLocation(@RequestParam String location) {
        List<DoctorDto> doctors = doctorServices.getAllDoctorsByLocation(location);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/date/doctors/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsByDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<DoctorDto> doctors = doctorServices.getAllDoctorsByDate(localDate);
        return ResponseEntity.ok(doctors);
    }

    // Additional endpoints for medical reports and appointments can be added here

    @PostMapping(value = "/{userId}/medical-report/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedicalReportDto> createMedicalReport(
            @Valid @RequestBody MedicalReportRequestDto medicalReportDto,
            @PathVariable Long userId
    ) {
        MedicalReportDto createdReport = doctorServices.createMedicalReport(medicalReportDto, userId);
        return ResponseEntity.ok(createdReport);
    }
    @PostMapping("/appointment/{appointmentId}/accept/")
    public ResponseEntity<String> acceptAppointment(@PathVariable Long appointmentId) {
        String response = doctorServices.acceptAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/appointment/{appointmentId}/cancel/")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long appointmentId) {
        String response = doctorServices.cancelAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/appointments/accepted/")
    public ResponseEntity<List<AppointmentDto>> allAcceptedAppointments() {
        List<AppointmentDto> appointments = doctorServices.allAcceptedAppointments();
        return ResponseEntity.ok(appointments);
    }
    @GetMapping("/appointments/rejected/")
    public ResponseEntity<List<AppointmentDto>> allRejectedAppointments() {
        List<AppointmentDto> appointments = doctorServices.allRejectedAppointments();
        return ResponseEntity.ok(appointments);
    }
    @PatchMapping("/appointment/{appointmentId}/reschedule/")
    public ResponseEntity<String> rescheduleAppointment(@PathVariable Long appointmentId,
                                                        @RequestParam String newAppointmentDate) {
        LocalDateTime newDate = LocalDateTime.parse(newAppointmentDate);
        String response = doctorServices.rescheduleAppointment(appointmentId, newDate);
        return ResponseEntity.ok(response);
    }
}
