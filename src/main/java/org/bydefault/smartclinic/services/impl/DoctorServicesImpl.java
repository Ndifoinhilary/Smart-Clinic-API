package org.bydefault.smartclinic.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.doctor.DoctorApplicationDto;
import org.bydefault.smartclinic.email.EmailService;
import org.bydefault.smartclinic.entities.ApplicationStatus;
import org.bydefault.smartclinic.entities.Doctor;
import org.bydefault.smartclinic.entities.User;
import org.bydefault.smartclinic.exception.ResourceNotFoundException;
import org.bydefault.smartclinic.exception.UserAlreadyExistException;
import org.bydefault.smartclinic.repository.DoctorRepository;
import org.bydefault.smartclinic.repository.UserRepository;
import org.bydefault.smartclinic.services.ImageService;
import org.bydefault.smartclinic.services.doctor.DoctorServices;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j

public class DoctorServicesImpl implements DoctorServices {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public DoctorApplicationDto submitApplication(DoctorApplicationDto applicationDto, MultipartFile idPhoto, MultipartFile certificate, MultipartFile shortVideo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (doctorRepository.existsByUserId(user.getId())){
            throw  new UserAlreadyExistException("You have already submitted a doctor application");
        }

        try {
            // Upload files
            String idPhotoUrl = imageService.storeFile(idPhoto, "doctor-applications/id-photos");
            String certificateUrl = imageService.storeFile(certificate, "doctor-applications/certificates");
            String videoUrl = imageService.storeFile(shortVideo, "doctor-applications/videos");

            // Create an application entity
            Doctor application = new Doctor();
            application.setUser(user);
            application.setLocation(applicationDto.getLocation());
            application.setHighersQualifications(applicationDto.getHighersQualifications());
            application.setAnyOtherQualifications(applicationDto.getAnyOtherQualifications());
            application.setIdPhoto(idPhotoUrl);
            application.setCertificate(certificateUrl);
            application.setShortVideo(videoUrl);
            application.setStatus(ApplicationStatus.PENDING);
            application.setSubmittedAt(LocalDateTime.now());
            application.setAccepted(false);


           var savedDoctor =  doctorRepository.save(application);
           emailService.sendDoctorApplicationEmail(user.getEmail(), savedDoctor, ApplicationStatus.PENDING);

            // Return DTO
            applicationDto.setIdPhoto(idPhotoUrl);
            applicationDto.setCertificate(certificateUrl);
            applicationDto.setShortVideo(videoUrl);

            return applicationDto;

        } catch (Exception e) {
            log.error("Error storing files: {}", e.getMessage());
            throw new RuntimeException("Failed to process application files", e);
        }
    }
}
