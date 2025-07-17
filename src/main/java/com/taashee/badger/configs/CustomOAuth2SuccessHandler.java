package com.taashee.badger.configs;

import com.taashee.badger.models.User;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.configs.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${app.uiBaseUrl:http://localhost:5173}")
    private String uiBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            String redirectUrl = uiBaseUrl + "/oauth-success?error=not_registered";
            response.sendRedirect(redirectUrl);
            return;
        }
        User user = userOpt.get();
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
        }
        String jwt = jwtUtil.generateToken(email);
        String redirectUrl = uiBaseUrl + "/oauth-success?token=" + jwt;
        response.sendRedirect(redirectUrl);
    }
} 