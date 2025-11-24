package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.audit.AuditLogDTO;
import com.ceedpods.crmbuild.entity.audit.AuditLog;
import org.springframework.stereotype.Component;

/**
 * Mapper for AuditLog entity and DTO
 */
@Component
public class AuditLogMapper {

    /**
     * Convert AuditLog entity to DTO
     */
    public AuditLogDTO toDTO(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .username(auditLog.getUsername())
                .userId(auditLog.getUserId())
                .userEmail(auditLog.getUserEmail())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .entityName(auditLog.getEntityName())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .timestamp(auditLog.getTimestamp())
                .status(auditLog.getStatus())
                .errorMessage(auditLog.getErrorMessage())
                .build();
    }

    /**
     * Convert AuditLog DTO to entity
     */
    public AuditLog toEntity(AuditLogDTO dto) {
        if (dto == null) {
            return null;
        }

        return AuditLog.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .userId(dto.getUserId())
                .userEmail(dto.getUserEmail())
                .action(dto.getAction())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .entityName(dto.getEntityName())
                .details(dto.getDetails())
                .ipAddress(dto.getIpAddress())
                .timestamp(dto.getTimestamp())
                .status(dto.getStatus())
                .errorMessage(dto.getErrorMessage())
                .build();
    }
}
