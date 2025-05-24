package org.bydefault.smartclinic.dtos.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VerificationResponseDto {
    private boolean success;
    private String message;
    private LocalDateTime verifiedAt;
}
