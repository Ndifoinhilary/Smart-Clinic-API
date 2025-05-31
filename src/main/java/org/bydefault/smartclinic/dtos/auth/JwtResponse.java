package org.bydefault.smartclinic.dtos.auth;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Response", description = "Response Data Transfer Object for JWT Authentication")
public class JwtResponse {
    private String token;
    private String message;
}
