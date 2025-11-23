package com.example.poker.controller;

import com.example.poker.controller.dto.LoginResponse;
import com.example.poker.controller.dto.UserRequest;
import com.example.poker.controller.dto.UserResponse;
import com.example.poker.domain.User;
import com.example.poker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        User created = userService.register(request.getUsername(), request.getPassword());
        return new UserResponse(created.getId(), created.getUsername());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody UserRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        User user = userService.findByName(request.getUsername()).orElseThrow();
        return new LoginResponse(user.getId(), user.getUsername(), token);
    }
}
