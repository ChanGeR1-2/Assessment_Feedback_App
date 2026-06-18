package com.dissertation.backend.app_users;

import com.dissertation.backend.app_users.dto.CreateUserRequest;
import com.dissertation.backend.app_users.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse user = userService.createUser(createUserRequest);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
