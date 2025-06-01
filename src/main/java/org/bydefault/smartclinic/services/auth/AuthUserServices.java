package org.bydefault.smartclinic.services.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.bydefault.smartclinic.dtos.auth.*;
import org.bydefault.smartclinic.dtos.common.ProfileDto;
import org.bydefault.smartclinic.dtos.user.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface AuthUserServices {
    RegisterUserDto register(RegisterUserDto registerUserDto);

    VerificationResponseDto verify(VerifyAccountDto verifyAccountDto);

    void resendVerificationCode(ResendCodeDto resentDto);

    String changePassword(PasswordUpdateDto passwordUpdateDto);

    JwtResponse login(LoginRequestDto loginRequestDto, HttpServletResponse response);

    UserDto currentUser();

    JwtResponse refreshToken(String refreshToken);

    String forgotPassword(ForgotPassword resetPasswordDto);

    ProfileDto updateProfile(ProfileDto profileDto, MultipartFile profileImage);

    ProfileDto getCurrentUserProfile();
}
