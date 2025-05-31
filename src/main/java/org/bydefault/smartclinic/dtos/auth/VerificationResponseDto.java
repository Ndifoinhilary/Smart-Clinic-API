package org.bydefault.smartclinic.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(name = "ResendCode", description = "ResendCode Data Transfer Object for Verification")
public class VerificationResponseDto {
    private boolean success;
    private String message;
    private LocalDateTime verifiedAt;
}
