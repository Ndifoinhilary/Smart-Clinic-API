package org.bydefault.smartclinic.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Doctor application", description = "Apply to become a doctor by submitting an application with required documents.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application submitted successfully",
                    content = @Content(schema = @Schema(implementation = DoctorApplicationDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(schema = @Schema()))
    })
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


    @Operation(summary = "Doctor add availability", description = "Add availability for a doctor to allow patients to book appointments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability added successfully",
                    content = @Content(schema = @Schema(implementation = AvailabilityDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/availability/")
    public ResponseEntity<AvailabilityDto> addAvailability(
            @Valid @RequestBody AvailabilityDto availabilityDto
    ) {
        return ResponseEntity.ok(doctorServices.addAvailability(availabilityDto));
    }

    @Operation(summary = "Doctor Update availability date", description = "Update availability for a doctor to allow patients to book appointments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability updated successfully",
                    content = @Content(schema = @Schema(implementation = AvailabilityDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/availability/{availabilityId}/")
    public ResponseEntity<AvailabilityDto> updateAvailability(@PathVariable Long availabilityId,
                                                              @Valid @RequestBody AvailabilityDto availabilityDto) {
        return ResponseEntity.ok(doctorServices.updateAvailability(availabilityId, availabilityDto));
    }

    @Operation(summary = "Doctor delete availability", description = "Delete availability for a doctor to allow patients to book appointments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "No specialties found",
                    content = @Content(schema = @Schema()))
    })
    @DeleteMapping("/availability/{availabilityId}/")
    public ResponseEntity<String> deleteAvailability(@PathVariable Long availabilityId) {
        String response = doctorServices.deleteAvailability(availabilityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get detail of a specific availability", description = "Retrieve details of a specific availability by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability found",
                    content = @Content(schema = @Schema(implementation = AvailabilityDto.class))),
            @ApiResponse(responseCode = "404", description = "Availability not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/availability/{availabilityId}/")
    public ResponseEntity<AvailabilityDto> getAvailabilityById(@PathVariable Long availabilityId) {
        AvailabilityDto availabilityDto = doctorServices.getAvailabilityById(availabilityId);
        return ResponseEntity.ok(availabilityDto);
    }

    @Operation(summary = "Get a doctor availability", description = "Retrieve all availability for a specific doctor by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability found",
                    content = @Content(schema = @Schema(implementation = AvailabilityDto.class))),
            @ApiResponse(responseCode = "404", description = "Availability not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/availability/doctor/{doctorId}/")
    public ResponseEntity<List<AvailabilityDto>> getAvailabilityByDoctor(@PathVariable Long doctorId) {
        List<AvailabilityDto> availabilityDtos = doctorServices.getAvailabilityByDoctor(doctorId);
        return ResponseEntity.ok(availabilityDtos);
    }

    @Operation(summary = "Filter doctors by availability day and time", description = "Retrieve all doctors available on a specific day and time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors found",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No doctors found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/availability/doctor/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsByAvailability(@RequestParam String day, @RequestParam String time) {
        LocalTime Localtime = LocalTime.parse(time);
        List<DoctorDto> availabilityDtos = doctorServices.getAllDoctorsByAvailability(day, Localtime);
        return ResponseEntity.ok(availabilityDtos);
    }


    @Operation(summary = "Filter doctors by specialty", description = "Retrieve all doctors by their specialty.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of specialties",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No specialties found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/specialty/doctors/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsBySpecialty(@RequestParam String specialtyName) {
        List<DoctorDto> doctors = doctorServices.getAllDoctorsBySpecialty(specialtyName);
        return ResponseEntity.ok(doctors);
    }

    @Operation(summary = "Filter doctors by location", description = "Retrieve all doctors by their location.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of doctors by location",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No doctors found for that location",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/location/doctors/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsByLocation(@RequestParam String location) {
        List<DoctorDto> doctors = doctorServices.getAllDoctorsByLocation(location);
        return ResponseEntity.ok(doctors);
    }


    @Operation(summary = "Filter doctors by availability date", description = "Retrieve all doctors available on a specific date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of specialties",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No specialties found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/date/doctors/")
    public ResponseEntity<List<DoctorDto>> getAllDoctorsByDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<DoctorDto> doctors = doctorServices.getAllDoctorsByDate(localDate);
        return ResponseEntity.ok(doctors);
    }

    // Additional endpoints for medical reports and appointments can be added here

    @Operation(summary = "Create medical report", description = "Create a medical report for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medical report created successfully",
                    content = @Content(schema = @Schema(implementation = MedicalReportDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping(value = "/{userId}/medical-report/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedicalReportDto> createMedicalReport(
            @Valid @RequestBody MedicalReportRequestDto medicalReportDto,
            @PathVariable Long userId
    ) {
        MedicalReportDto createdReport = doctorServices.createMedicalReport(medicalReportDto, userId);
        return ResponseEntity.ok(createdReport);
    }

    @Operation(summary = "Accept appointment", description = "Accept an appointment request from a patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment accepted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "An appointment not found",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/appointment/{appointmentId}/accept/")
    public ResponseEntity<String> acceptAppointment(@PathVariable Long appointmentId) {
        String response = doctorServices.acceptAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel appointment", description = "Cancel an appointment request from a patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "An appointment not found",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/appointment/{appointmentId}/cancel/")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long appointmentId) {
        String response = doctorServices.cancelAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "View all accepted appointment", description = "Retrieve all accepted appointments for a doctor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of accepted appointments",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "No accepted appointments found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/appointments/accepted/")
    public ResponseEntity<List<AppointmentDto>> allAcceptedAppointments() {
        List<AppointmentDto> appointments = doctorServices.allAcceptedAppointments();
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "All rejected appointment", description = "Retrieve all rejected appointments for a doctor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of rejected appointments",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "No specialties found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/appointments/rejected/")
    public ResponseEntity<List<AppointmentDto>> allRejectedAppointments() {
        List<AppointmentDto> appointments = doctorServices.allRejectedAppointments();
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Reschedule appointment", description = "Reschedule an existing appointment to a new date and time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment rescheduled successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/appointment/{appointmentId}/reschedule/")
    public ResponseEntity<String> rescheduleAppointment(@PathVariable Long appointmentId,
                                                        @RequestParam String newAppointmentDate) {
        LocalDateTime newDate = LocalDateTime.parse(newAppointmentDate);
        String response = doctorServices.rescheduleAppointment(appointmentId, newDate);
        return ResponseEntity.ok(response);
    }
}
