package org.bydefault.smartclinic.dtos.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
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
