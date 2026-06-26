package com.security.microservice.security.oauth2;

import com.security.microservice.entity.User;
import com.security.microservice.enums.AuthProvider;
import com.security.microservice.enums.Role;
import com.security.microservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        DefaultOAuth2UserService delegate =
                new DefaultOAuth2UserService();

        OAuth2User oAuth2User =
                delegate.loadUser(userRequest);

        // Google User Information
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, name));

        return oAuth2User;
    }

    private User createGoogleUser(String email, String name) {

        User user = User.builder()
                .username(email.split("@")[0])
                .email(email)
                .password("")
                .role(Role.LEARNER)
                .provider(AuthProvider.GOOGLE)
                .enabled(true)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }

}