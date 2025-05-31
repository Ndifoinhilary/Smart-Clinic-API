package org.bydefault.smartclinic.dtos.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "ForgotPassword", description = "ForgotPassword Data Transfer Object")
public class ForgotPassword {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String confirmPassword;

    @NotBlank(message = "Token is required")
    @Size(min = 6, message = "Token must be at least 6 characters long")
    private String token;
}
