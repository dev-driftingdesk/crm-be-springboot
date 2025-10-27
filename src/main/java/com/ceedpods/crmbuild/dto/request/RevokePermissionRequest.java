package com.ceedpods.crmbuild.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokePermissionRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Permission code is required")
    private String permissionCode;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}