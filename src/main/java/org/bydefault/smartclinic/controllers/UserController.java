package org.bydefault.smartclinic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bydefault.smartclinic.dtos.common.AppointmentDto;
import org.bydefault.smartclinic.dtos.common.DoctorDto;
import org.bydefault.smartclinic.dtos.common.MedicalReportDto;
import org.bydefault.smartclinic.services.users.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserServices userServices;

    @Operation(summary = "Make an appointment", description = "Make an appointment with a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment made successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content(schema = @Schema()))})
    @PostMapping("/{doctorId}/appointments/")
    public ResponseEntity<String> makeAppointment(@PathVariable Long doctorId, @RequestBody LocalDateTime appointmentDate, @RequestBody String description) {
        String response = userServices.makeAppointment(doctorId, appointmentDate, description);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View doctor profile", description = "View a doctor's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content(schema = @Schema()))})
    @GetMapping("/doctors/{doctorId}/profile/")
    public ResponseEntity<DoctorDto> viewDoctorProfile(@PathVariable Long doctorId) {
        return ResponseEntity.ok(userServices.viewDoctorProfile(doctorId));
    }

    @Operation(summary = "All doctors", description = "Retrieve all doctors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all doctors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No doctors found", content = @Content(schema = @Schema()))})
    @GetMapping("/doctors/")
    public ResponseEntity<List<DoctorDto>> allDoctors() {
        return ResponseEntity.ok(userServices.allDoctor());
    }

    @Operation(summary = "All past appointments", description = "Retrieve all past appointments of a patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of past appointments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No past appointments found", content = @Content(schema = @Schema()))})
    @GetMapping("/appointments/past/")
    public ResponseEntity<List<AppointmentDto>> viewPastAppointments() {
        return ResponseEntity.ok(userServices.viewPastAppointments());
    }

    @Operation(summary = "View appointment details", description = "View the details of a specific appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(schema = @Schema()))})
    @GetMapping("/appointments/{appointmentId}/")
    public ResponseEntity<AppointmentDto> viewAppointmentDetails(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(userServices.viewAppointmentDetails(appointmentId));
    }

    @Operation(summary = "View medical report details", description = "View the details of a specific medical report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medical report details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "Medical report not found", content = @Content(schema = @Schema()))})
    @GetMapping("/appointments/{medicalReportId}/medical-report/")
    public ResponseEntity<?> viewMedicalReport(@PathVariable Long medicalReportId) {
        return ResponseEntity.ok(userServices.viewMedicalReport(medicalReportId));
    }

    @Operation(summary = "List of medical reports", description = "Retrieve all medical reports of a patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of medical reports retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No medical reports found",
                    content = @Content(schema = @Schema()))})
    @GetMapping("/medical-reports/")
    public ResponseEntity<List<MedicalReportDto>> viewAllMedicalReports() {
        return ResponseEntity.ok(userServices.viewAllMedicalReports());
    }


    @Operation(summary = "View all appointment", description = "Retrieve all appointments of a patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of appointments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No appointments found",
                    content = @Content(schema = @Schema()))})
    @GetMapping("/appointments/")
    public ResponseEntity<List<AppointmentDto>> viewAllAppointments() {
        return ResponseEntity.ok(userServices.viewAllAppointments());
    }

    @Operation(summary = "Filter doctors by specialty", description = "Retrieve all doctors by specialty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of doctors by specialty retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "No doctors found for the specified specialty",
                    content = @Content(schema = @Schema()))})
    @GetMapping("/doctors/specialty/{specialtyName}/")
    public ResponseEntity<?> allDoctorsBySpecialty(@PathVariable String specialtyName) {
        return ResponseEntity.ok(userServices.allDoctorBySpecialty(specialtyName));
    }

}
