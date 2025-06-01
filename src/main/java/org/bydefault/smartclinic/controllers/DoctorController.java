package org.bydefault.smartclinic.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.doctor.DoctorApplicationDto;
import org.bydefault.smartclinic.exception.ResourceNotFoundException;
import org.bydefault.smartclinic.services.ImageService;
import org.bydefault.smartclinic.services.doctor.DoctorServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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




}
