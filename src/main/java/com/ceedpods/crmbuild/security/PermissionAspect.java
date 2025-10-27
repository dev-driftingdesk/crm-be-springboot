package com.ceedpods.crmbuild.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {
    
    private final CustomPermissionEvaluator permissionEvaluator;
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        
        String permissionCode = requirePermission.value();
        String fallbackPermission = requirePermission.fallback();
        
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, permissionCode);
        
        // Check fallback permission if primary permission is not granted
        if (!hasPermission && !fallbackPermission.isEmpty()) {
            hasPermission = permissionEvaluator.hasPermission(authentication, fallbackPermission);
            log.debug("Primary permission {} denied, checking fallback permission {}: {}", 
                permissionCode, fallbackPermission, hasPermission);
        }
        
        if (!hasPermission) {
            log.warn("Access denied for user {} to permission {}", 
                permissionEvaluator.getCurrentKeycloakId(authentication), permissionCode);
            throw new AccessDeniedException("Insufficient permissions: " + permissionCode);
        }
        
        log.debug("Permission {} granted for user {}", 
            permissionCode, permissionEvaluator.getCurrentKeycloakId(authentication));
        
        return joinPoint.proceed();
    }
    
    @Around("@annotation(requireAnyPermission)")
    public Object checkAnyPermission(ProceedingJoinPoint joinPoint, RequireAnyPermission requireAnyPermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        
        String[] permissionCodes = requireAnyPermission.value();
        
        boolean hasAnyPermission = permissionEvaluator.hasAnyPermission(authentication, permissionCodes);
        
        if (!hasAnyPermission) {
            log.warn("Access denied for user {} to any of permissions: {}", 
                permissionEvaluator.getCurrentKeycloakId(authentication), String.join(", ", permissionCodes));
            throw new AccessDeniedException("Insufficient permissions. Required one of: " + String.join(", ", permissionCodes));
        }
        
        log.debug("Permission granted for user {} with one of: {}", 
            permissionEvaluator.getCurrentKeycloakId(authentication), String.join(", ", permissionCodes));
        
        return joinPoint.proceed();
    }
}