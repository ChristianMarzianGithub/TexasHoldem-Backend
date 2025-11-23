package com.example.poker.controller.dto;

public class LoginResponse {
    private final String userId;
    private final String username;
    private final String token;

    public LoginResponse(String userId, String username, String token) {
        this.userId = userId;
        this.username = username;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
