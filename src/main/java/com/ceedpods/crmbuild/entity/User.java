package com.ceedpods.crmbuild.entity;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = AppConstants.MongoDB.COLLECTION_USERS)
public class User {
    
    @Id
    private String id;

    @Indexed(unique = true)
    private String keycloakId;

    @Indexed(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean enabled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
