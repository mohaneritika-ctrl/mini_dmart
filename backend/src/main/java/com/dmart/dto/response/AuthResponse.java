package com.dmart.dto.response;

import com.dmart.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private java.time.LocalDateTime lastLoginAt;
}