package com.example.poker.controller.dto;

public class LoginResponse {
    private final String id;
    private final String username;
    private final String token;

    public LoginResponse(String id, String username, String token) {
        this.id = id;
        this.username = username;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
