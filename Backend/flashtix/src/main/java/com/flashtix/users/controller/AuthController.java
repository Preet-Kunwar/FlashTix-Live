package com.flashtix.users.controller;

import com.flashtix.security.jwt.JwtUtils;
import com.flashtix.users.entity.Role;
import com.flashtix.users.entity.User;
import com.flashtix.users.repository.RoleRepository;
import com.flashtix.users.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("[AUTH] Register attempt for username='{}', email='{}'", request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("[AUTH] Registration FAILED — username '{}' already taken", request.getUsername());
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AUTH] Registration FAILED — email '{}' already in use", request.getEmail());
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Default Role is not found in the database."));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        log.info("[AUTH] ✅ User '{}' registered successfully with role ROLE_USER", request.getUsername());

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("[AUTH] Login attempt for username='{}'", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String accessToken = jwtUtils.generateAccessToken(userDetails);
            String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getUsername());

            log.info("[AUTH] ✅ Login successful for username='{}', roles={}",
                    loginRequest.getUsername(), userDetails.getAuthorities());

            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));

        } catch (BadCredentialsException e) {
            log.warn("[AUTH] ❌ Login FAILED for username='{}' — bad credentials", loginRequest.getUsername());
            return ResponseEntity.status(401).body("Error: Invalid username or password.");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        log.debug("[AUTH] Token refresh attempt");

        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtUtils.generateAccessToken(userDetails);

            log.info("[AUTH] ✅ Token refreshed for username='{}'", username);
            return ResponseEntity.ok(new JwtResponse(newAccessToken, requestRefreshToken));
        }

        log.warn("[AUTH] ❌ Token refresh FAILED — invalid or expired refresh token");
        return ResponseEntity.badRequest().body("Invalid or expired Refresh Token");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless — client discards the token. No server-side action needed.
        // Future: add token to Redis blacklist here.
        log.info("[AUTH] Logout endpoint called (stateless JWT — client handles token disposal)");
        return ResponseEntity.ok("Logged out successfully.");
    }

    // ─── DTOs ────────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    @Data
    public static class JwtResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";

        public JwtResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}