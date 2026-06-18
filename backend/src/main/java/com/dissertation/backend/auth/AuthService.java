package com.dissertation.backend.auth;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.auth.dto.LoginRequest;
import com.dissertation.backend.auth.dto.LoginResponse;
import com.dissertation.backend.auth.exceptions.InvalidPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        AppUser user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UserNotFoundException(
                        loginRequest.email()
                ));
        boolean passwordMatches = passwordEncoder.matches(
                loginRequest.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidPasswordException();
        }

        return new LoginResponse (
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );

    }
}
