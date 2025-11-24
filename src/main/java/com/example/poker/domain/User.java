package com.example.poker.domain;

import java.util.UUID;

public class User {
    private final String id;
    private final String username;
    private final String passwordHash;

    public User(String username, String passwordHash) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
