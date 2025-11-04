package com.ceedpods.crmbuild.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entity representing a password reset token.
 * Stores verification codes sent to users for password reset functionality.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;

    /**
     * Email address of the user requesting password reset
     */
    @Indexed
    private String email;

    /**
     * 6-digit verification code sent to user's email
     */
    private String code;

    /**
     * Timestamp when the token expires (15 minutes from creation)
     */
    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiresAt;

    /**
     * Flag indicating if the token has been used
     */
    private boolean used;

    /**
     * Timestamp when the token was used
     */
    private LocalDateTime usedAt;

    /**
     * Timestamp when the token was created
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Checks if the token is expired
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token is valid (not used and not expired)
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !used && !isExpired();
    }

    /**
     * Marks the token as used
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}
