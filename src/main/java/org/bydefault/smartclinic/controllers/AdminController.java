package org.bydefault.smartclinic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.bydefault.smartclinic.dtos.admin.*;
import org.bydefault.smartclinic.entities.Role;
import org.bydefault.smartclinic.services.admin.AdminServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin management operations")
public class AdminController {

    private final AdminServices services;


    @GetMapping("users/")
    @Operation(summary = "Get all user", description = "Paginated list of users with optional role filtering and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of users",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Page<UserDto>> getAllUsers(@RequestParam(required = false) Role role,
                                                     @RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                                                     @RequestParam(defaultValue = "") List<String> sortList,
                                                     @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        return ResponseEntity.ok(services.getAllUsers(role, page, size, sortList, sortOrder.toString()));

    }


    @Operation(summary = "Get user by ID", description = "Retrieve a user's details using their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("user/{id}/")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(services.getUserById(id));
    }

    @Operation(summary = "Accept a doctor request", description = "Accept a doctor's request to join the platform")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor accepted",
                    content = @Content(schema = @Schema(implementation = DoctorDto.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("doctor/{id}/")
    public ResponseEntity<DoctorDto> acceptDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(services.acceptDoctor(id));
    }

    @Operation(summary = "Reject a doctor", description = "Reject a doctor's request to join the platform")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor rejected"),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("doctors/{id}")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(services.rejectDoctor(id));
    }

    @Operation(summary = "Delete a user", description = "Delete a user from the system using their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return new ResponseEntity<>(services.deleteUser(id), HttpStatus.NO_CONTENT);
    }


    @Operation(summary = "All appointments", description = "Retrieve all appointments in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of appointments",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "No appointments found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("appointments/")
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
        return ResponseEntity.ok(services.getAllAppointments());
    }

    @Operation(summary = "Get an appointment", description = "Retrieve an appointment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of appointment",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("appointment/{id}/")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(services.getAppointmentById(id));
    }

    @Operation(summary = "Delete an appointment", description = "Cancel an appointment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("appointment/{id}/delete/")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(services.deleteAppointment(id));
    }

    @Operation(summary = "All appointment for a doctor", description = "Retrieve all appointments for a specific doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of appointments for the doctor",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found or no appointments available",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("appointments/doctor/{id}/")
    public ResponseEntity<List<AppointmentDto>> getAppointmentByDoctorId(@PathVariable Long id) {
        return ResponseEntity.ok(services.getAppointmentByDoctorId(id));
    }

    @Operation(summary = "Get appointment by user ID", description = "Retrieve all appointments for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of appointments for the user",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found or no appointments available",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("appointments/user/{id}/")
    public ResponseEntity<List<AppointmentDto>> getAppointmentByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(services.getAppointmentByPatientId(id));
    }


    @Operation(summary = "All medical reports", description = "Retrieve all medical reports in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of medical reports",
                    content = @Content(schema = @Schema(implementation = MedicalReportDto.class))),
            @ApiResponse(responseCode = "404", description = "No medical reports found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("medical-reports/")
    public ResponseEntity<List<MedicalReportDto>> medicalReports() {
        return ResponseEntity.ok(services.getReport());
    }

    @Operation(summary = "Create specialty", description = "Create a new specialty for doctors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialty created successfully",
                    content = @Content(schema = @Schema(implementation = SpecialtyDto.class))),
            @ApiResponse(responseCode = "404", description = "Invalid request",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("specialties/")
    public ResponseEntity<SpecialtyDto> createSpecialty(@Valid @RequestBody SpecialtyDto specialtyDto) {
        return ResponseEntity.ok(services.createSpecialty(specialtyDto));
    }

    @Operation(summary = "Update a specialty", description = "Update an existing specialty by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialty updated successfully",
                    content = @Content(schema = @Schema(implementation = SpecialtyDto.class))),
            @ApiResponse(responseCode = "404", description = "Specialty not found",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("specialties/{id}/")
    public ResponseEntity<SpecialtyDto> updateSpecialty(@PathVariable Long id, @Valid @RequestBody SpecialtyDto specialtyDto) {
        return ResponseEntity.ok(services.updateSpecialty(id, specialtyDto));
    }

    @Operation(summary = "Delete specialty", description = "Delete a specialty by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialty deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Specialty not found",
                    content = @Content(schema = @Schema()))
    })
    @DeleteMapping("specialties/{id}/")
    public ResponseEntity<?> deleteSpecialty(@PathVariable Long id) {
        return ResponseEntity.ok(services.deleteSpecialty(id));
    }

    @Operation(summary = "Get all specialty", description = "Retrieve all specialties available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of specialties",
                    content = @Content(schema = @Schema(implementation = SpecialtyDto.class))),
            @ApiResponse(responseCode = "404", description = "No specialties found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("specialties/")
    public ResponseEntity<List<SpecialtyDto>> getAllSpecialties() {
        return ResponseEntity.ok(services.getAllSpecialties());
    }

    @Operation(summary = "Get  specialty by ID", description = "Retrieve a specialty by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialty found",
                    content = @Content(schema = @Schema(implementation = SpecialtyDto.class))),
            @ApiResponse(responseCode = "404", description = "Specialty not found",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("specialties/{id}/doctors/")
    public ResponseEntity<List<DoctorDto>> getDoctorsBySpecialtyId(@PathVariable Long id) {
        return ResponseEntity.ok(services.getAllDoctorsBySpecialtyId(id));
    }
}
