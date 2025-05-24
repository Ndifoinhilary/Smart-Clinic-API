package org.bydefault.smartclinic.services.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.dtos.auth.*;
import org.bydefault.smartclinic.dtos.user.UserDto;
import org.bydefault.smartclinic.email.EmailService;
import org.bydefault.smartclinic.entities.Role;
import org.bydefault.smartclinic.entities.User;
import org.bydefault.smartclinic.exception.*;
import org.bydefault.smartclinic.mappers.UserMapper;
import org.bydefault.smartclinic.repository.UserRepository;
import org.bydefault.smartclinic.securityConfig.JwtConfig;
import org.bydefault.smartclinic.securityConfig.JwtServices;
import org.bydefault.smartclinic.services.AuthUserServices;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserServiceImpl implements AuthUserServices {

    //    in minutes
    private static final int CODE_EXPIRY_MINUTES = 5;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServices jwtServices;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Override
    @Transactional
    public RegisterUserDto register(RegisterUserDto registerUserDto) {
        var email = registerUserDto.getEmail();
        if (userRepository.existsByEmail(email)) {
            log.error("User with email {} already exists", email);
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
        if (!registerUserDto.getPassword().equals(registerUserDto.getConfirmPassword())) {
            log.error("Passwords do not match");
            throw new PasswordNotMatchException("Passwords do not match");
        }
        var user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        user.setIsVerified(Boolean.FALSE);
        user.setCode(generateVerificationCode());
        user.setCodeExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        user.setRole(Role.PATIENT);
        userRepository.save(user);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendVerificationEmail(email, user);
                log.info("Welcome email sent to {}", email);
            } catch (Exception e) {
                log.error("Failed to send welcome email to {}: {}", email, e.getMessage());

            }
        });
        registerUserDto.setId(user.getId());
        registerUserDto.setEmail(user.getEmail());
        return registerUserDto;
    }

    @Override
    @Transactional
    public VerificationResponseDto verify(VerifyAccountDto verifyAccountDto) {
        User user = userRepository.findByEmail(verifyAccountDto.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return VerificationResponseDto.builder()
                    .success(false)
                    .message("Account already verified")
                    .build();
        }

        if (user.getCode() == null) {
            throw new VerificationException("No verification code found. Please request a new one.");
        }

        if (user.getCodeExpiresAt() != null && user.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification code has expired for email: {}", verifyAccountDto.getEmail());
            user.setCode(null);
            throw new ExpiredVerificationCodeException("Verification code has expired. Please request a new one.");
        }

        if (!user.getCode().equals(verifyAccountDto.getCode())) {
            log.warn("Invalid verification code attempt for email: {}", verifyAccountDto.getEmail());
            throw new VerificationException("Invalid verification code");
        }

        user.setIsVerified(true);
        user.setVerifiedAt(LocalDateTime.now());
        user.setCode(null);
        user.setCodeExpiresAt(null);

        userRepository.save(user);

        log.info("Account verified successfully for email: {}", verifyAccountDto.getEmail());

        return VerificationResponseDto.builder()
                .success(true)
                .message("Account verified successfully")
                .verifiedAt(user.getVerifiedAt())
                .build();
    }

    @Transactional
    @Override
    public void resendVerificationCode(ResendCodeDto resendDto) {
        User user = userRepository.findByEmail(resendDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + resendDto.getEmail()));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new VerificationException("Account is already verified");
        }

        String newCode = generateVerificationCode();
        user.setCode(newCode);
        user.setCodeExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), user);
            log.info("Verification code resent to email: {}", resendDto.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend verification email to {}: {}", resendDto.getEmail(), e.getMessage());
            throw new EmailException("Failed to send verification email");
        }
    }


    @Override
    public String changePassword(PasswordUpdateDto passwordUpdateDto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        var newPassword = passwordUpdateDto.getNewPassword();
        var confirmPassword = passwordUpdateDto.getConfirmPassword();
        if (!passwordEncoder.matches(passwordUpdateDto.getCurrentPassword(), user.getPassword())) {
            throw new PasswordNotMatchException("Current password is incorrect.");
        }
        if (!confirmPassword.equals(newPassword)) {
            throw new PasswordNotMatchException("New password and confirm password is incorrect");

        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new PasswordNotMatchException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password changed successfully";
    }

    @Override
    public JwtResponse login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email: {}", loginRequestDto.getEmail());
            log.warn("Authentication exception: {}", ex.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
        var user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User " + loginRequestDto.getEmail() + " not found"));
        if (!user.getIsVerified()) {
            log.warn("User {} is not verified", user.getEmail());
            throw new VerificationException("Please verify your account before logging in");
        }
        var accessToken = jwtServices.generateAccessToken(user);
        var refreshToken = jwtServices.generateRefreshToken(user);
        var cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh/");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration() / 1000);
        cookie.setSecure(true);
        response.addCookie(cookie);
        var jwtToken = new JwtResponse();
        jwtToken.setToken(accessToken);
        jwtToken.setMessage("Login successful");
        return jwtToken;
    }

    @Override
    public UserDto currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtServices.validateJwtToken(refreshToken)) {
            throw new RuntimeException("Invalid JWT token");
        }
        var userId = jwtServices.getUserIdFromJwtToken(refreshToken);
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var accessToken = jwtServices.generateRefreshToken(user);
        var jwtToken = new JwtResponse();
        jwtToken.setToken(accessToken);
        return jwtToken;
    }

    @Override
    public String forgotPassword(ForgotPassword forgotPassword) {
        var code = forgotPassword.getToken();

        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset code"));

        if (user.getCodeExpiresAt() == null || user.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Password reset code has expired for email: {}", user.getEmail());
            user.setCode(null);
            user.setCodeExpiresAt(null);
            userRepository.save(user);
            throw new ExpiredVerificationCodeException("Password reset code has expired. Please request a new one.");
        }

        if (!forgotPassword.getPassword().equals(forgotPassword.getConfirmPassword())) {
            log.warn("Passwords do not match for email: {}", user.getEmail());
            throw new PasswordNotMatchException("Password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(forgotPassword.getPassword()));
        user.setCode(null);
        user.setCodeExpiresAt(null);
        userRepository.save(user);

        log.info("Password reset successfully for email: {}", user.getEmail());
        return "Password reset successfully. Please go ahead and login with your new password.";
    }

}
