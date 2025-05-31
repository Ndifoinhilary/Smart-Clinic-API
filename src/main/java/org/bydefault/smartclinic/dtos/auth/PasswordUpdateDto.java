package org.bydefault.smartclinic.dtos.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "PasswordUpdate", description = "PasswordUpdate Data Transfer Object for Authentication")
public class PasswordUpdateDto {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "New password is required")
    private String newPassword;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
