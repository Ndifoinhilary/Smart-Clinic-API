package org.bydefault.smartclinic.dtos.admin;

import lombok.Data;
import org.bydefault.smartclinic.entities.Role;

import java.util.Set;
@Data
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
