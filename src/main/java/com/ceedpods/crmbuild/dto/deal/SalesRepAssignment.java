package com.ceedpods.crmbuild.dto.deal;

import com.ceedpods.crmbuild.enums.SalesRepPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesRepAssignment {

    @NotBlank(message = "Sales rep ID is required")
    private String id; // User ID (UUID)

    @NotNull(message = "Position is required")
    private SalesRepPosition position; // Primary, Co-primary, Consultant
}
