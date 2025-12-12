package com.ceedpods.crmbuild.entity.notification;

import com.ceedpods.crmbuild.constants.AppConstants;
import lombok.*;
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
@Document(collection = AppConstants.MongoDB.COLLECTION_USER_NOTIFICATION_STATUS)
@CompoundIndexes({
    @CompoundIndex(name = "user_notification_idx", def = "{'userId': 1, 'notificationId': 1}", unique = true),
    @CompoundIndex(name = "user_read_idx", def = "{'userId': 1, 'read': 1}")
})
public class UserNotificationStatus {

    @Id
    private String id;

    @Indexed
    private String notificationId;

    @Indexed
    private String userId;

    @Builder.Default
    private boolean read = false;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}
