package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignManagerRequest {
    

    @NotBlank(message = "Manager ID is required")
    private String managerId;

    @Size(max = 100, message = "Territory must not exceed 100 characters")
    private String territory;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}