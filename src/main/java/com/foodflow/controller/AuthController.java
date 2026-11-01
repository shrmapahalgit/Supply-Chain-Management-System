package com.foodflow.controller;

import com.foodflow.model.LoginRequest;
import com.foodflow.model.RegisterRequest;
import com.foodflow.model.User;
import com.foodflow.model.UserResponse;
import com.foodflow.service.UserOperations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserOperations userOperations;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserOperations userOperations, AuthenticationManager authenticationManager) {
        this.userOperations = userOperations;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userOperations.register(request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Account registered successfully. You can now log in.");
        response.put("user", UserResponse.fromUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        String email = request.getEmail().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userOperations.findByEmail(email).orElse(null);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Login successful");
        response.put("authenticated", true);
        response.put("user", UserResponse.fromUser(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        String email = auth.getName();
        User user = userOperations.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("user", UserResponse.fromUser(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/csrf")
    public Map<String, Object> csrf(CsrfToken csrfToken) {
        if (csrfToken == null) {
            return Map.of("token", "", "headerName", "X-XSRF-TOKEN", "parameterName", "_csrf");
        }
        return Map.of(
            "token", csrfToken.getToken(),
            "headerName", csrfToken.getHeaderName(),
            "parameterName", csrfToken.getParameterName()
        );
    }
}
