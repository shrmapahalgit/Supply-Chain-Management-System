package com.foodflow.service;

import com.foodflow.model.RegisterRequest;
import com.foodflow.model.User;

import java.util.Optional;

/**
 * Service contract defining user lifecycle and registration operations.
 * Demonstrates interface abstraction and loose coupling.
 */
public interface UserOperations {
    User register(RegisterRequest request);
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);
    int countUsers();
}
