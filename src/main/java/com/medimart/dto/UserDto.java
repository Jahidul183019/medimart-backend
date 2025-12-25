// src/main/java/com/medimart/dto/UserDto.java
package com.medimart.dto;

import com.medimart.model.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String createdAt;   // 👈 string for frontend

    public static UserDto fromEntity(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setFullName(
                (u.getFirstName() == null ? "" : u.getFirstName()) + " " +
                        (u.getLastName() == null ? "" : u.getLastName())
        );
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setRole("CUSTOMER"); // or from a future role field

        // safely convert to string
        dto.setCreatedAt(
                u.getCreatedAt() == null ? null : u.getCreatedAt().toString()
        );

        return dto;
    }
}
