package com.medimart.service.impl;

import com.medimart.dto.*;
import com.medimart.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse loginCustomer(LoginRequest request) {
        return dummyAuthResponse("CUSTOMER", request.getEmail());
    }

    @Override
    public AuthResponse loginAdmin(LoginRequest request) {
        return dummyAuthResponse("ADMIN", request.getEmail());
    }

    @Override
    public AuthResponse signupCustomer(SignupRequest request) {
        String email = request.getEmail();   // <-- only this getter now
        return new AuthResponse(
                1L,
                "New",
                "Customer",
                email,
                "CUSTOMER",
                "dummy-jwt-token-signup"
        );
    }

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        return new ForgotPasswordResponse("OTP sent (demo)");
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        return new VerifyOtpResponse(true, "dummy-reset-token");
    }

    @Override
    public ForgotPasswordResponse resetPassword(ResetPasswordRequest request) {
        return new ForgotPasswordResponse("Password reset (demo)");
    }

    private AuthResponse dummyAuthResponse(String role, String email) {
        return new AuthResponse(
                1L,
                "Demo",
                "User",
                email != null ? email : "demo@example.com",
                role,
                "dummy-jwt-token"
        );
    }
}
