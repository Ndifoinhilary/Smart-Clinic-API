package org.bydefault.smartclinic.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "ResendCode", description = "ResendCode Data Transfer Object for Verification")
public class ResendCodeDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
