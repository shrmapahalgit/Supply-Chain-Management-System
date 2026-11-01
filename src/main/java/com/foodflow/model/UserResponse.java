package com.foodflow.model;

import java.time.LocalDateTime;

/**
 * Safe public representation of an authenticated user.
 * Sensitive data such as password hashes are strictly excluded.
 */
public class UserResponse {
    private long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String roleLabel;
    private String status;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(long id, String fullName, String email, String phone,
                        String role, String roleLabel, String status, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.roleLabel = roleLabel;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static UserResponse fromUser(User user) {
        if (user == null) return null;
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole(),
            user.getRoleLabel(),
            user.getStatus(),
            user.getCreatedAt()
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public void setRoleLabel(String roleLabel) {
        this.roleLabel = roleLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
