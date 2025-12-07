package com.ceedpods.crmbuild.dto.lead;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"totalLeadValue", "totalCommission", "averageDealSize", "conversionProbability",
                     "openDealsCount", "closedDealsCount", "wonDealsCount", "lostDealsCount", "criticalItems"})
public class LeadStatisticsDTO {

    private BigDecimal totalLeadValue; // Sum of all product values from all deals
    private BigDecimal totalCommission; // Sum of all deal commissions
    private BigDecimal averageDealSize; // Average deal value (totalLeadValue / totalDeals)
    private Double conversionProbability; // Probability based on lead status (0-100%)
    private long openDealsCount; // Count of deals with status OPEN/PENDING/NEGOTIATION
    private long closedDealsCount; // Count of deals with status WON or LOST
    private long wonDealsCount; // Count of deals with status WON
    private long lostDealsCount; // Count of deals with status LOST
    private List<String> criticalItems; // List of critical issues/alerts
}
