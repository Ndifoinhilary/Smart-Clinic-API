package org.bydefault.smartclinic.dtos.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link org.bydefault.smartclinic.entities.Profile}
 */
@Data
@Schema(name = "Profile", description = "Profile information model")
public class ProfileDto implements Serializable {
    private Long id;
    private String description;
    private String imageUrl;
    private String phoneNumber;
    private String address;
}
