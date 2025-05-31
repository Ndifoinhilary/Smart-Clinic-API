package org.bydefault.smartclinic.dtos.auth;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "LoginRequest Data Transfer Object for Authentication")
public class LoginRequestDto {
    @NotBlank(message = "This field is required")
    @Email(message = "Enter a valid email please")
    private String email;

    @Size(message = "Your password should be between 6 to 25.", min = 6, max = 25)
    private String password;
}
