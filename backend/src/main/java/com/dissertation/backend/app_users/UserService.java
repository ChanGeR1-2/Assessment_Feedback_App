package com.dissertation.backend.app_users;

import com.dissertation.backend.app_users.dto.CreateUserRequest;
import com.dissertation.backend.app_users.dto.UserResponse;
import com.dissertation.backend.app_users.exceptions.EmailExistsException;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toResponse(user);
    }

    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.email())) {
            throw new EmailExistsException(createUserRequest.email());
        }

        AppUser user = new AppUser();
        user.setFullName(createUserRequest.fullName());
        user.setEmail(createUserRequest.email());
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.password()));
        user.setRole(createUserRequest.role());
        AppUser savedUser = userRepository.save(user);

        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),  savedUser.getRole());
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
