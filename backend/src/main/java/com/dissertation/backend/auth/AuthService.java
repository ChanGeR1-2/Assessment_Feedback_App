package com.dissertation.backend.auth;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.auth.dto.LoginRequest;
import com.dissertation.backend.auth.dto.LoginResponse;
import com.dissertation.backend.auth.exceptions.InvalidPasswordException;
import com.dissertation.backend.config.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidPasswordException("Incorrect email or password");
        }

        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidPasswordException("Incorrect email or password"));

        return new LoginResponse(
                jwtService.generateToken(user),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole());
    }
}