package org.bydefault.smartclinic.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.bydefault.smartclinic.entities.Role;

@Data
@Schema(
        description = "User Data Transfer Object",
        title = "User",
        name = "User")
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String lastName;
    private String firstName;
    private Role role;
    private boolean isVerified;
    private String verifiedAt;
    private ProfileDto profile;
}
