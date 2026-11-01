package com.foodflow.model;

import java.time.LocalDateTime;

/**
 * Represents an authenticated user of the FoodFlow platform.
 * Demonstrates OOP concepts:
 * - Inheritance: Extends the abstract Person base class
 * - Encapsulation: Private state with validated accessors
 * - Polymorphism: Overrides getRoleLabel() from Person
 */
public class User extends Person {
    private String password;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public User() {
        super();
        this.role = "ROLE_USER";
        this.status = "ACTIVE";
    }

    public User(long id, String fullName, String email, String phone,
                String password, String role, String status, LocalDateTime createdAt) {
        super(id, fullName, email, phone);
        setPassword(password);
        setRole(role);
        setStatus(status);
        this.createdAt = createdAt;
    }

    /**
     * Polymorphic implementation of Person.getRoleLabel().
     * Runtime behavior adapts to the assigned security role.
     */
    @Override
    public String getRoleLabel() {
        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
            return "System Administrator";
        }
        return "Supply Chain Operator";
    }

    public String getFullName() {
        return getContactName();
    }

    public void setFullName(String fullName) {
        setContactName(fullName);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            this.role = "ROLE_USER";
        } else {
            this.role = role.trim().toUpperCase();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            this.status = "ACTIVE";
        } else {
            this.status = status.trim().toUpperCase();
        }
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
