package com.security.microservice.security.oauth2;

import com.security.microservice.entity.User;
import com.security.microservice.enums.AuthProvider;
import com.security.microservice.enums.Role;
import com.security.microservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        DefaultOAuth2UserService defaultOAuth2UserService =
                new DefaultOAuth2UserService();

        OAuth2User oAuth2User =
                defaultOAuth2UserService.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, name));

        return oAuth2User;
    }

    private User createGoogleUser(String email, String name) {

        String username = email.split("@")[0];

        if (userRepository.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis();
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.LEARNER)
                .provider(AuthProvider.GOOGLE)
                .enabled(true)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }
}