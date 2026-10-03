package com.flashtix.users.repository;

import com.flashtix.users.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Used by DataInitializer to check if roles exist on startup
    Boolean existsByName(String name);

    // Useful for when you want to assign a specific role to a user later
    Optional<Role> findByName(String name);
}