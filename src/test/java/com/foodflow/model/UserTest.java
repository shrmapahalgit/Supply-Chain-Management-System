package com.foodflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Inheritance & Polymorphism: User extends Person and overrides getRoleLabel()")
    void userInheritsPersonAndDemonstratesPolymorphism() {
        Person user = new User(1, "Ananya Mehta", "ananya@foodflow.internal", "+91 98765 11001",
                "hashedSecret123", "ROLE_USER", "ACTIVE", LocalDateTime.now());

        // Polymorphic call via Person reference
        assertEquals("Supply Chain Operator", user.getRoleLabel());
        assertEquals("Ananya Mehta", user.getContactName());
        assertEquals("ananya@foodflow.internal", user.getEmail());
    }

    @Test
    @DisplayName("Polymorphism: Admin role produces administrator role label")
    void adminProducesAdministratorRoleLabel() {
        User admin = new User(2, "Super Admin", "admin@foodflow.internal", null,
                "hashedSecret123", "ROLE_ADMIN", "ACTIVE", LocalDateTime.now());

        assertEquals("System Administrator", admin.getRoleLabel());
    }

    @Test
    @DisplayName("Encapsulation: Password cannot be blank")
    void passwordCannotBeBlank() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> user.setPassword(""));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword(null));
    }

    @Test
    @DisplayName("Encapsulation & Security: UserResponse never exposes password")
    void userResponseExcludesPassword() {
        User user = new User(1, "Rohit Verma", "rohit@foodflow.internal", "+91 98765 11002",
                "$2a$10$superSecretHashValue", "ROLE_USER", "ACTIVE", LocalDateTime.now());

        UserResponse dto = UserResponse.fromUser(user);
        assertNotNull(dto);
        assertEquals("Rohit Verma", dto.getFullName());
        assertEquals("rohit@foodflow.internal", dto.getEmail());
        assertEquals("Supply Chain Operator", dto.getRoleLabel());
    }

    @Test
    @DisplayName("Account status check helper")
    void activeStatusCheck() {
        User activeUser = new User();
        activeUser.setStatus("ACTIVE");
        assertTrue(activeUser.isActive());

        activeUser.setStatus("INACTIVE");
        assertFalse(activeUser.isActive());
    }
}
