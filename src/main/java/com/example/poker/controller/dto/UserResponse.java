package com.example.poker.controller.dto;

public class UserResponse {
    private final String userId;
    private final String username;

    public UserResponse(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
