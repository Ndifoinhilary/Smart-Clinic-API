package org.bydefault.smartclinic.securityConfig;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.entities.Role;
import org.bydefault.smartclinic.entities.User;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
@Slf4j
public class JwtServices {

    private JwtConfig jwtConfig;

    public String generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration());
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshTokenExpiration());
    }

    private String generateToken(User user, long tokenExpirationInSeconds) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("verified", user.getIsVerified())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationInSeconds))
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            var claims = getClaims(token);
            var verified = claims.get("verified", Boolean.class);
            boolean isVerified = verified != null && verified;
            boolean isNotExpired = claims.getExpiration().after(new Date());
            log.debug("Token validation result: isVerified={}, isNotExpired={}", isVerified, isNotExpired);
            return  isVerified && isNotExpired;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromJwtToken(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public Role getUserRoleFromJwtToken(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }
}
