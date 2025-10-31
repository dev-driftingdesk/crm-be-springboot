package com.ceedpods.crmbuild.dto.deal;

import com.ceedpods.crmbuild.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DealDTO extends BaseDTO {

    private String id; // UUID as string (dealId)
    private String dealName;
    private List<String> productIds;
    private List<String> salesReps;
    private String leadId;
}
