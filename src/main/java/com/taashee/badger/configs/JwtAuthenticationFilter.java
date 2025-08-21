package com.taashee.badger.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import com.taashee.badger.services.UserService;
import com.taashee.badger.models.User;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = null;
        
        // First check Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Remove "Bearer " prefix
            System.out.println("JWT found in Authorization header: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
        }
        
        // If no Authorization header, check cookies
        if (jwt == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("badger_jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    System.out.println("JWT found in cookie: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                    break;
                }
            }
        }
        
        if (jwt != null) {
            System.out.println("Processing JWT token...");
            if (jwtUtil.validateToken(jwt)) {
                System.out.println("JWT token is valid");
                try {
                    String email = jwtUtil.getSubject(jwt);
                    System.out.println("JWT subject (email): " + email);
                    User user = userService.findByEmail(email);
                    if (user != null) {
                        System.out.println("User found: " + user.getEmail() + " with roles: " + user.getRoles());
                        List<String> roles = user.getRoles().stream()
                            .map(role -> "ROLE_" + role.toUpperCase())
                            .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                email, null, roles.stream()
                                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                                    .collect(Collectors.toList()));
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        System.out.println("Security context set for user: " + email);
                    } else {
                        System.out.println("User not found for email: " + email);
                    }
                } catch (Exception e) {
                    // Log the error but don't fail the request
                    System.err.println("Error processing JWT token: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("JWT token is invalid");
            }
        } else {
            System.out.println("No JWT token found in request");
        }
        
        filterChain.doFilter(request, response);
    }
} 