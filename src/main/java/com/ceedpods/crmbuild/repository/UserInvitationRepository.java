package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.UserInvitation;
import com.ceedpods.crmbuild.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvitationRepository extends MongoRepository<UserInvitation, String> {
    
    Optional<UserInvitation> findByInvitationToken(String invitationToken);
    
    Optional<UserInvitation> findByEmail(String email);
    
    List<UserInvitation> findByInvitedBy(String invitedBy);
    
    List<UserInvitation> findByRole(UserRole role);
    
    @Query("{ 'used': false, 'expiresAt': { '$gte': ?0 }, 'deleted': false }")
    List<UserInvitation> findValidInvitations(LocalDateTime currentTime);
    
    @Query("{ 'expiresAt': { '$lt': ?0 }, 'used': false, 'deleted': false }")
    List<UserInvitation> findExpiredInvitations(LocalDateTime currentTime);
    
    @Query("{ 'used': false, 'emailSent': true, 'expiresAt': { '$gte': ?0 }, 'deleted': false }")
    List<UserInvitation> findPendingInvitations(LocalDateTime currentTime);
    
    @Query("{ 'email': ?0, 'used': false, 'expiresAt': { '$gte': ?1 }, 'deleted': false }")
    Optional<UserInvitation> findValidInvitationByEmail(String email, LocalDateTime currentTime);
    
    @Query("{ 'invitationToken': ?0, 'used': false, 'expiresAt': { '$gte': ?1 }, 'deleted': false }")
    Optional<UserInvitation> findValidInvitationByToken(String token, LocalDateTime currentTime);
    
    @Query("{ 'assignedManagerId': ?0, 'deleted': false }")
    List<UserInvitation> findInvitationsByManagerId(String managerId);
    
    @Query("{ 'emailSent': false, 'deleted': false }")
    List<UserInvitation> findUnsentInvitations();
    
    @Query("{ 'used': false, 'emailSent': true, 'lastReminderSent': { '$lt': ?0 }, 'reminderCount': { '$lt': ?1 }, 'expiresAt': { '$gte': ?2 }, 'deleted': false }")
    List<UserInvitation> findInvitationsNeedingReminder(LocalDateTime reminderThreshold, int maxReminders, LocalDateTime currentTime);
    
    boolean existsByEmailAndUsedFalse(String email);
    
    boolean existsByInvitationToken(String invitationToken);
}