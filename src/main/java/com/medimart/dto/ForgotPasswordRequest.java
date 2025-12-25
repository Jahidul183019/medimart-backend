package com.medimart.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
    private String phone;
}
