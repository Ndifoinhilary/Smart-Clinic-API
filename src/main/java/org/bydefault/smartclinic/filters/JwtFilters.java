package org.bydefault.smartclinic.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.securityConfig.JwtServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class JwtFilters extends OncePerRequestFilter {
    private final JwtServices jwtServices;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/v1/auth/register/",
            "/api/v1/auth/login/",
            "/api/v1/auth/forgot-password/",
            "/api/v1/auth/resend-verification/",
            "/api/v1/auth/verify/",
            "/api/v1/auth/refresh-token/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldSkip = EXCLUDED_PATHS.contains(path);
        if (shouldSkip) {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
        }
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request headers for protected endpoint: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtServices.validateJwtToken(token)) {
                var role = jwtServices.getUserRoleFromJwtToken(token);
                var userId = jwtServices.getUserIdFromJwtToken(token);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful for user: {}", userId);
            } else {
                log.warn("Invalid JWT token for request: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
