package com.flashtix.users.repository;

import com.flashtix.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Required for Spring Security to find a user during login
    Optional<User> findByUsername(String username);

    // Good to have for validation during registration
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}