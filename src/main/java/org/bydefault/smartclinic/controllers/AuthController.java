package org.bydefault.smartclinic.controllers;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.auth.*;
import org.bydefault.smartclinic.services.AuthUserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final AuthUserServices userServices;

    @PostMapping("register/")
    public ResponseEntity<RegisterUserDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto, UriComponentsBuilder uriBuilder) {
        var user = userServices.register(registerUserDto);
        var uri = uriBuilder.path("/api/v1/auth/{id}/").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(user);
    }

    @PostMapping("verify/")
    public ResponseEntity<VerificationResponseDto> verifyAccount(@Valid @RequestBody VerifyAccountDto verifyDto) {
        VerificationResponseDto response = userServices.verify(verifyDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("login/")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        log.info("Login request received for email: {}", loginRequestDto.getEmail());
        return ResponseEntity.ok(userServices.login(loginRequestDto, response));
    }

    @PostMapping("refresh-token/")
    public ResponseEntity<JwtResponse> refreshToken(@CookieValue(value = "refresh_token") String token) {
        return ResponseEntity.ok(userServices.refreshToken(token));
    }

    @PostMapping("resend-verification/")
    public ResponseEntity<Map<String, String>> resendVerificationCode(@Valid @RequestBody ResendCodeDto resendDto) {
        userServices.resendVerificationCode(resendDto);

        Map<String, String> response = Map.of(
                "message", "Verification code sent successfully",
                "email", resendDto.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("change-password/")
    public ResponseEntity<?> changePassword( @Valid @RequestBody PasswordUpdateDto password) {
        return ResponseEntity.ok(userServices.changePassword(password));
    }

    @PostMapping("forgot-password/")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPassword resetPasswordDto) {
        return ResponseEntity.ok(userServices.forgotPassword(resetPasswordDto));
    }


}
