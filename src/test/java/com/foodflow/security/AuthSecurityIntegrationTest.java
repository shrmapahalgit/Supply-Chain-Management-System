package com.foodflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.model.LoginRequest;
import com.foodflow.model.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Security: Unauthenticated calls to /api/dashboard return 401 Unauthorized")
    void unauthenticatedCallsToDashboardReturn401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error", containsString("Unauthorized")));
    }

    @Test
    @DisplayName("Security: Unauthenticated calls to /api/products return 401 Unauthorized")
    void unauthenticatedCallsToProductsReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Public Access: Auth pages and static stylesheets are accessible without authentication")
    void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/login.html")).andExpect(status().isOk());
        mockMvc.perform(get("/signup.html")).andExpect(status().isOk());
        mockMvc.perform(get("/styles.css")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("End-to-End: Register user, login, access protected API with session, and logout")
    void fullAuthLifecycleTest() throws Exception {
        String uniqueEmail = "operator_" + System.currentTimeMillis() + "@foodflow.test";
        RegisterRequest registerReq = new RegisterRequest("Test Operator", uniqueEmail, "SecretPass123!", "+91 99999 11111");

        // 1. Register new user (defaults to ROLE_USER)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(uniqueEmail))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.user.roleLabel").value("Supply Chain Operator"));

        // 2. Reject duplicate email registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already exists")));

        // 3. Reject login with wrong password
        LoginRequest badLogin = new LoginRequest(uniqueEmail, "WrongPassword!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());

        // 4. Successful login produces active session
        LoginRequest goodLogin = new LoginRequest(uniqueEmail, "SecretPass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goodLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.user.email").value(uniqueEmail))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        assert session != null;

        // 5. Access protected /api/dashboard with session
        mockMvc.perform(get("/api/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isNumber())
                .andExpect(jsonPath("$.suppliers").isNumber());

        // 6. Access /api/auth/me returns current user
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.user.email").value(uniqueEmail));
    }
}
