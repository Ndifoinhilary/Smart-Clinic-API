package org.bydefault.smartclinic.dtos.auth;


import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String message;
}
