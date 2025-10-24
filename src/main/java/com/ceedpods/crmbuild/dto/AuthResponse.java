package com.ceedpods.crmbuild.dto;

import com.ceedpods.crmbuild.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = AppConstants.Keycloak.TOKEN_TYPE_BEARER;

    private long expiresIn;
    private String email;
}
