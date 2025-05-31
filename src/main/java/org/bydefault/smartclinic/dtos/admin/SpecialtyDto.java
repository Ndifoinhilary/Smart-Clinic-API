package org.bydefault.smartclinic.dtos.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "Specialty", description = "Specialty Data Transfer Object")
public class SpecialtyDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Description is required")
    private String description;
}
