package org.bydefault.smartclinic.services;

import jakarta.servlet.http.HttpServletResponse;
import org.bydefault.smartclinic.dtos.auth.*;
import org.bydefault.smartclinic.dtos.user.UserDto;

public interface AuthUserServices {
    RegisterUserDto  register(RegisterUserDto registerUserDto);

    VerificationResponseDto verify(VerifyAccountDto verifyAccountDto);

    void  resendVerificationCode(ResendCodeDto resentDto);

    String changePassword(PasswordUpdateDto passwordUpdateDto);

    JwtResponse login(LoginRequestDto loginRequestDto, HttpServletResponse response);

    UserDto currentUser();

    JwtResponse refreshToken(String refreshToken);

    String forgotPassword(ForgotPassword resetPasswordDto);


}
