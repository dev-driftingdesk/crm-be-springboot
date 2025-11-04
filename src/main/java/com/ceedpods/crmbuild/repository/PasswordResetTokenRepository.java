package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PasswordResetToken entity operations.
 */
@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    /**
     * Find a password reset token by email and code
     * @param email User's email address
     * @param code Verification code
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByEmailAndCode(String email, String code);
}
