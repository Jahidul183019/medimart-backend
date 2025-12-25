package com.medimart.service;

import com.medimart.dto.*;

public interface AuthService {

    AuthResponse loginCustomer(LoginRequest request);

    AuthResponse loginAdmin(LoginRequest request);

    AuthResponse signupCustomer(SignupRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

    ForgotPasswordResponse resetPassword(ResetPasswordRequest request);
}
