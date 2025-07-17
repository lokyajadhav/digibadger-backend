package com.taashee.badger.controllers;

import com.taashee.badger.models.User;
import com.taashee.badger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.taashee.badger.configs.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import com.taashee.badger.services.EmailVerificationService;
import com.taashee.badger.models.TermsOfService;
import com.taashee.badger.models.UserTermsAgreement;
import com.taashee.badger.repositories.TermsOfServiceRepository;
import com.taashee.badger.repositories.UserTermsAgreementRepository;

@Tag(name = "Authentication", description = "APIs for user registration, login, and logout. Author: Lokya Naik")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private TermsOfServiceRepository termsOfServiceRepository;
    @Autowired
    private UserTermsAgreementRepository userTermsAgreementRepository;

    @Operation(summary = "Register a new user", description = "Creates a new user account. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content),
        @ApiResponse(responseCode = "400", description = "Validation error or email exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration payload",
                required = true,
                content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), "Email is required", null, "Email is required"
            ));
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), "Password must be at least 8 characters", null, "Password must be at least 8 characters"
            ));
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            return ResponseEntity.badRequest().body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), "First name is required", null, "First name is required"
            ));
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            return ResponseEntity.badRequest().body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), "Last name is required", null, "Last name is required"
            ));
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), "Email already exists", null, "Email already exists"
            ));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        userService.saveUser(user);
        emailVerificationService.createVerificationTokenForUser(user);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(
            HttpStatus.OK.value(), "User registered successfully. Please check your email to verify your account.", null, null
        ));
    }

    @Operation(summary = "Login user", description = "Authenticates user and sets JWT cookie. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT cookie set", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Login payload (email, password)",
                required = true,
                content = @Content(schema = @Schema(example = "{\"email\":\"user@example.com\",\"password\":\"secret\"}"))
            )
            @RequestBody Map<String, String> loginRequest,
            HttpServletResponse response) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), "Email and password are required.", null, "Email and password are required."
            ));
        }
        User user = userService.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(), "Invalid email or password.", null, "Invalid email or password."
            ));
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new com.taashee.badger.models.ApiResponse<>(
                HttpStatus.FORBIDDEN.value(), "Account not verified. Please check your email.", null, "Account not verified."
            ));
        }
        String jwt = jwtUtil.generateToken(user.getEmail());
        Cookie cookie = new Cookie("badger_jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to false for development (HTTP)
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 hour
        response.addCookie(cookie);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Login successful");
        data.put("roles", user.getRoles());
        data.put("user", user); // Add user object for frontend
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(
            HttpStatus.OK.value(), "Login successful", data, null
        ));
    }

    @Operation(summary = "Logout user", description = "Clears JWT cookie. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logged out", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("badger_jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to false for development (HTTP)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        Cookie jsessionCookie = new Cookie("JSESSIONID", null);
        jsessionCookie.setHttpOnly(true);
        jsessionCookie.setSecure(false); // Set to false for development (HTTP)
        jsessionCookie.setPath("/");
        jsessionCookie.setMaxAge(0);
        response.addCookie(jsessionCookie);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Logged out");
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(
            HttpStatus.OK.value(), "Logged out", data, null
        ));
    }

    @Operation(summary = "Accept Terms of Service", description = "User accepts the latest Terms of Service. Author: Lokya Naik")
    @PostMapping("/user/terms/accept")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<UserTermsAgreement>> acceptTerms(@RequestBody Map<String, Long> body, @RequestParam Long userId) {
        Long termsId = body.get("termsId");
        TermsOfService tos = termsOfServiceRepository.findById(termsId).orElse(null);
        if (tos == null) {
            return ResponseEntity.status(404).body(new com.taashee.badger.models.ApiResponse<>(404, "ToS not found", null, null));
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(new com.taashee.badger.models.ApiResponse<>(404, "User not found", null, null));
        }
        UserTermsAgreement agreement = new UserTermsAgreement();
        agreement.setUser(user);
        agreement.setTerms(tos);
        agreement.setAgreed(true);
        agreement.setAgreedAt(java.time.LocalDateTime.now());
        userTermsAgreementRepository.save(agreement);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(200, "Agreement saved", agreement, null));
    }

    @Operation(summary = "Get current authenticated user", description = "Returns the current user's profile info if authenticated. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current user info returned", content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new com.taashee.badger.models.ApiResponse<>(401, "Not authenticated", null, null));
        }
        String email = (String) authentication.getPrincipal();
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new com.taashee.badger.models.ApiResponse<>(401, "User not found", null, null));
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("roles", user.getRoles());
        userInfo.put("enabled", user.isEnabled());
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(200, "Current user info", userInfo, null));
    }

    // For demo: login is handled by HTTP Basic Auth, or you can add JWT in next sprint
} 