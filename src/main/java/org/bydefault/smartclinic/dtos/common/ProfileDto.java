package org.bydefault.smartclinic.dtos.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link org.bydefault.smartclinic.entities.Profile}
 */
@Data
@Schema(name = "Profile", description = "Profile information model")
public class ProfileDto implements Serializable {
    @JsonIgnore
    private Long id;
    @NotBlank(message = "Brief about you is required")
    private String description;

    private String imageUrl;

    @NotBlank(message = "Please enter your phone number with country code please")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format")
    private String phoneNumber;


    @NotBlank(message = "Give your home address please")
    private String address;
}
