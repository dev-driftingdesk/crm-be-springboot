package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseConfigRequest {

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Private Key ID is required")
    private String privateKeyId;

    @NotBlank(message = "Private Key is required")
    private String privateKey;

    @NotBlank(message = "Client Email is required")
    private String clientEmail;

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Auth URI is required")
    private String authUri;

    @NotBlank(message = "Token URI is required")
    private String tokenUri;

    @NotBlank(message = "Auth Provider X509 Cert URL is required")
    private String authProviderX509CertUrl;

    @NotBlank(message = "Client X509 Cert URL is required")
    private String clientX509CertUrl;

    @NotBlank(message = "Universe Domain is required")
    private String universeDomain;
}
