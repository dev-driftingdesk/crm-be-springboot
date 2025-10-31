package com.ceedpods.crmbuild.entity.deal;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private List<String> productIds; // List of product IDs (one or more)

    private List<String> salesReps; // List of user IDs (sales representatives)

    private String leadId; // Single lead ID (required)
}
