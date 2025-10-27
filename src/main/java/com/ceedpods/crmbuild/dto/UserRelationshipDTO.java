package com.ceedpods.crmbuild.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRelationshipDTO extends BaseDTO {
    
    private String id;
    private String managerId;
    private String salesRepId;
    private String assignedBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;
    
    private boolean active;
    private String territory;
    private String notes;
    
    // For relationship changes
    private String previousManagerId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime relationshipStartDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime relationshipEndDate;
    
    private String endedBy;
    private String endReason;
    
    // Related entities
    private UserDTO manager;
    private UserDTO salesRep;
    private UserDTO assignedByUser;
    private UserDTO endedByUser;
    private UserDTO previousManager;
    
    // Computed fields
    private long relationshipDurationDays;
    private boolean isCurrentRelationship;
    private String relationshipStatus;
}