package com.ceedpods.crmbuild.entity.deal;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.deal.DealProduct;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.DealStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_DEALS)
public class Deal extends BaseEntity {

    @Id
    private String id; // UUID as primary ID (dealId)

    private String dealName;

    private DealStatus status; // Deal status (OPEN, WON, LOST, PENDING, NEGOTIATION)

    private BigDecimal dealValue; // Calculated total value of the deal from products

    private BigDecimal commission; // Commission amount for this deal

    private List<DealProduct> products; // List of products with productId, packageType, quantity

    private List<SalesRepAssignment> salesRepresentatives; // List of sales rep assignments with userId and role

    private String leadId; // Associated lead ID (required)
}
