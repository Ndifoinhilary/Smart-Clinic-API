package org.bydefault.smartclinic.services.doctor;

import org.bydefault.smartclinic.dtos.doctor.DoctorApplicationDto;
import org.springframework.web.multipart.MultipartFile;

public interface DoctorServices {
    DoctorApplicationDto submitApplication(
            DoctorApplicationDto applicationDto,
            MultipartFile idPhoto,
            MultipartFile certificate,
            MultipartFile shortVideo
           );
}
