package com.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private String refreshToken;

    private UserInfo userInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Set<String> roles;
        private LocalDateTime lastLogin;
    }

    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, UserInfo userInfo) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build();
    }
}
