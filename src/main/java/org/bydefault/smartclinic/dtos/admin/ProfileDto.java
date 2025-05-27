package org.bydefault.smartclinic.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link org.bydefault.smartclinic.entities.Profile}
 */
@Data
public class ProfileDto implements Serializable {
    private Long id;
    private String description;
    private String imageUrl;
    private String phoneNumber;
    private String address;
}
