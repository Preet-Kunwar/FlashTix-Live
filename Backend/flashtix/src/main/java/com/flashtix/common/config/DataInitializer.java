package com.flashtix.common.config;

import com.flashtix.users.entity.Role;
import com.flashtix.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            if (!roleRepository.existsByName("ROLE_USER")) {
                roleRepository.save(new Role(null, "ROLE_USER"));
            }
            if (!roleRepository.existsByName("ROLE_ADMIN")) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }
        };
    }
}