package org.bydefault.smartclinic.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.auth.*;
import org.bydefault.smartclinic.dtos.common.ProfileDto;
import org.bydefault.smartclinic.services.auth.AuthUserServices;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth/")
@AllArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthController {
    private final AuthUserServices userServices;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    @Operation(summary = "Register user", description = "Create a new user in the system with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User create successfully",
                    content = @Content(schema = @Schema(implementation = RegisterUserDto.class))),
            @ApiResponse(responseCode = "404", description = "Fail to create user",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("register/")
    public ResponseEntity<RegisterUserDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto, UriComponentsBuilder uriBuilder) {
        var user = userServices.register(registerUserDto);
        var uri = uriBuilder.path("/api/v1/auth/{id}/").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(user);
    }

    @Operation(summary = "Verify user account", description = "Verify user account and the code is in the email you use to create an account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account verify successfully",
                    content = @Content(schema = @Schema(implementation = VerificationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Fail to verify user",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("verify/")
    public ResponseEntity<VerificationResponseDto> verifyAccount(@Valid @RequestBody VerifyAccountDto verifyDto) {
        VerificationResponseDto response = userServices.verify(verifyDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login user", description = "Log in with the details you use to create an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login  successfully",
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))),
            @ApiResponse(responseCode = "404", description = "Fail to login user",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("login/")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        log.info("Login request received for email: {}", loginRequestDto.getEmail());
        return ResponseEntity.ok(userServices.login(loginRequestDto, response));
    }

    @Operation(summary = "New access token", description = "User the old access token to get a new access token to sent a request with it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New access token sent successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Fail to generate new access token",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("refresh-token/")
    public ResponseEntity<JwtResponse> refreshToken(@CookieValue(value = "refresh_token") String token) {
        return ResponseEntity.ok(userServices.refreshToken(token));
    }

    @Operation(summary = "Resend code", description = "Resend code to verify the user account or your account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code resend successfully",
                    content = @Content(schema = @Schema(implementation = ResendCodeDto.class))),
            @ApiResponse(responseCode = "404", description = "Fail to resend  user the code",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("resend-verification/")
    public ResponseEntity<Map<String, String>> resendVerificationCode(@Valid @RequestBody ResendCodeDto resendDto) {
        userServices.resendVerificationCode(resendDto);

        Map<String, String> response = Map.of(
                "message", "Verification code sent successfully",
                "email", resendDto.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change your password", description = "Change your password by given the old and the new one you want to change")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password change  successfully",
                    content = @Content(schema = @Schema(implementation = PasswordUpdateDto.class))),
            @ApiResponse(responseCode = "404", description = "Fail to change password",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("change-password/")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordUpdateDto password) {
        return ResponseEntity.ok(userServices.changePassword(password));
    }

    @Operation(summary = "Forgot password", description = "Use your email you use to create an account to reset your password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content(schema = @Schema(implementation = ForgotPassword.class))),
            @ApiResponse(responseCode = "404", description = "Fail to reset password",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("forgot-password/")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPassword resetPasswordDto) {
        return ResponseEntity.ok(userServices.forgotPassword(resetPasswordDto));
    }

    @Operation(summary = "Current user profile", description = "View your profile ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieve  successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Fail to get profile",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("me/")
    public ResponseEntity<?> currentUser() {
        return ResponseEntity.ok(userServices.currentUser());
    }

    @GetMapping("profile/")
    public ResponseEntity<ProfileDto> currentUserProfile() {
        return ResponseEntity.ok(userServices.getCurrentUserProfile());
    }

    @Operation(summary = "Update profile with image", description = "Update your profile  and add image if you want ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Fail to Update profile",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "update/profile/image/")
    public ResponseEntity<ProfileDto> updateProfile(
            @RequestPart("profile") String profileJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            ProfileDto profileDto = objectMapper.readValue(profileJson, ProfileDto.class);

            Set<ConstraintViolation<ProfileDto>> violations = validator.validate(profileDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            ProfileDto updatedProfile = userServices.updateProfile(profileDto, profileImage);
            return ResponseEntity.ok(updatedProfile);

        } catch (JsonProcessingException e) {
            log.error("Error parsing profile JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ConstraintViolationException e) {
            log.error("Validation errors: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update profile without image", description = "Update your profile  without image ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = ProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "Fail to Update profile",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "update/profile/")
    public ResponseEntity<ProfileDto> updateProfileWithoutImage(@Valid @RequestBody ProfileDto profileDto) {
        ProfileDto updatedProfile = userServices.updateProfile(profileDto, null);
        return ResponseEntity.ok(updatedProfile);
    }


}
