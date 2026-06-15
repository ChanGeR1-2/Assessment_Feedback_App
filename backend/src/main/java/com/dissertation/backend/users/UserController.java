package com.dissertation.backend.users;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserRepository userRepository;
    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(user.getId(), user.getFullName(), user.getEmail()))
                .toList();
    }

    @PostMapping
    public UserResponse addUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        User user = new User();
        user.setFullName(createUserRequest.fullName());
        user.setEmail(createUserRequest.email());
        user.setPasswordHash(createUserRequest.password());
        userRepository.save(user);

        return new UserResponse(user.getId(), user.getFullName(), user.getEmail());
    }
}
