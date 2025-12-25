package com.medimart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;   // "ADMIN" / "CUSTOMER"
    private String token;  // JWT token
}
