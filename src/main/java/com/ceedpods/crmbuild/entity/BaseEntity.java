package com.ceedpods.crmbuild.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity implements Persistable<String> {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
    private boolean deleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public void markAsDeleted(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Determines if this entity is new (hasn't been persisted yet).
     * An entity is considered new if createdAt is null, which means it hasn't been saved to the database yet.
     * This allows @CreatedDate and @CreatedBy annotations to work correctly even when the ID is manually set.
     */
    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }

    // Audit fields are now automatically handled by Spring Data Auditing
    // @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy annotations
    // isNew() implementation ensures @CreatedDate and @CreatedBy work when ID is manually set
}