package com.medimart.controller;

import com.medimart.dto.AuthResponse;
import com.medimart.dto.ForgotPasswordRequest;
import com.medimart.dto.ForgotPasswordResponse;
import com.medimart.dto.LoginRequest;
import com.medimart.dto.ResetPasswordRequest;
import com.medimart.dto.SignupRequest;
import com.medimart.dto.VerifyOtpRequest;
import com.medimart.dto.VerifyOtpResponse;
import com.medimart.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ----------------- CUSTOMER LOGIN -----------------
    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginCustomer(@RequestBody LoginRequest request) {
        AuthResponse response = authService.loginCustomer(request);
        return ResponseEntity.ok(response);
    }

    // ----------------- ADMIN LOGIN -----------------
    // POST /api/auth/admin/login
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> loginAdmin(@RequestBody LoginRequest request) {
        AuthResponse response = authService.loginAdmin(request);
        return ResponseEntity.ok(response);
    }

    // ----------------- SIGNUP (CUSTOMER) -----------------
    // POST /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupCustomer(@RequestBody SignupRequest request) {
        AuthResponse response = authService.signupCustomer(request);
        return ResponseEntity.ok(response);
    }

    // ----------------- FORGOT PASSWORD -----------------
    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    // ----------------- VERIFY OTP -----------------
    // POST /api/auth/verify-otp
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    // ----------------- RESET PASSWORD -----------------
    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<ForgotPasswordResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        ForgotPasswordResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
