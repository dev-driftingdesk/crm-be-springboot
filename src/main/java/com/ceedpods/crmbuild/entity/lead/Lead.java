package com.ceedpods.crmbuild.entity.lead;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_LEADS)
public class Lead extends BaseEntity {

    @Id
    private String id; // UUID as primary ID

    private LeadOriginatedFrom originatedFrom; // Enum: facebook, whatsapp, instagram, email

    private String leadName;

    private String company;

    private String companyAddress;

    private String companyWebsite;

    private List<Map<String, String>> communication; // Array of key-value pairs, e.g., [{"phone": "987987987"}]

    private String platform;

    private String contactNumber;

    private String dealId; // Optional field - assigning a dealId is not mandatory
}
