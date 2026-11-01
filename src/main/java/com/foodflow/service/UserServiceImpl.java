package com.foodflow.service;

import com.foodflow.model.RegisterRequest;
import com.foodflow.model.User;
import com.foodflow.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserOperations, UserDetailsService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email '" + email + "' already exists");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Real BCrypt hashing
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Strict rule: Public registration always defaults to ROLE_USER. No hardcoded or self-assigned admin.
        User user = new User(
            0,
            request.getFullName().trim(),
            email,
            request.getPhone() == null || request.getPhone().isBlank() ? null : request.getPhone().trim(),
            encodedPassword,
            "ROLE_USER",
            "ACTIVE",
            null
        );

        return repository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return repository.findByEmail(email.trim().toLowerCase());
    }

    @Override
    public Optional<User> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public int countUsers() {
        return repository.count();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("No active user found with email: " + email));

        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        } else if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
            .disabled(!user.isActive())
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .build();
    }
}
