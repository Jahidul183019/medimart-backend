package com.medimart.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String emailOrPhone;
    private String otpCode;
}
