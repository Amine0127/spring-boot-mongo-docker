package com.example.spring_boot_mongodb_docker.repository;

import com.example.spring_boot_mongodb_docker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    void deleteByUsername(String username);
    boolean existsByUsername(String username);
    // Add to UserRepository.java
    Optional<User> findByEmail(String email);

}
