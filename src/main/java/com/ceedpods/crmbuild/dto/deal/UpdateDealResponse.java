package com.ceedpods.crmbuild.dto.deal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simplified response DTO for update deal operation.
 * Contains only essential fields: dealId, dealName, dealValue, updatedAt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"dealId", "dealName", "dealValue", "updatedAt"})
public class UpdateDealResponse {

    private String dealId;

    private String dealName;

    private BigDecimal dealValue;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}
