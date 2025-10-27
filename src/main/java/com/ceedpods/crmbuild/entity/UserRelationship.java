package com.ceedpods.crmbuild.entity;

import com.ceedpods.crmbuild.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_USER_RELATIONSHIPS)
@CompoundIndexes({
    @CompoundIndex(name = "manager_reports_idx", def = "{'managerId' : 1, 'active' : 1}"),
    @CompoundIndex(name = "salesrep_manager_idx", def = "{'salesRepId' : 1, 'active' : 1}")
})
public class UserRelationship extends BaseEntity {
    
    @Id
    private String id;
    
    @Indexed
    private String managerId; // Keycloak ID of manager
    
    @Indexed  
    private String salesRepId; // Keycloak ID of sales rep
    
    private String assignedBy; // Admin who made this assignment
    private LocalDateTime assignedAt;
    private boolean active = true;
    private String territory;
    private String notes;
    
    // For relationship changes
    private String previousManagerId;
    private LocalDateTime relationshipStartDate;
    private LocalDateTime relationshipEndDate;
    private String endedBy;
    private String endReason;
    
    public void endRelationship(String endedBy, String reason) {
        this.active = false;
        this.relationshipEndDate = LocalDateTime.now();
        this.endedBy = endedBy;
        this.endReason = reason;
    }
}