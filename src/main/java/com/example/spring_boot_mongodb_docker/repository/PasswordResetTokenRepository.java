package com.example.spring_boot_mongodb_docker.repository;

import com.example.spring_boot_mongodb_docker.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUsername(String username);
}

