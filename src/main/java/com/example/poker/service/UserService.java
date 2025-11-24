package com.example.poker.service;

import com.example.poker.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, User> usersByName = new ConcurrentHashMap<>();
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    public synchronized User register(String username, String password) {
        if (usersByName.containsKey(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        User user = new User(username, hash(password));
        usersById.put(user.getId(), user);
        usersByName.put(username, user);
        return user;
    }

    public synchronized String login(String username, String password) {
        User user = usersByName.get(username);
        if (user == null || !user.getPasswordHash().equals(hash(password))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, user.getId());
        return token;
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    public Optional<User> findByName(String username) {
        return Optional.ofNullable(usersByName.get(username));
    }

    public Optional<User> findByToken(String token) {
        String userId = activeTokens.get(token);
        return Optional.ofNullable(userId).map(usersById::get);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm not available", e);
        }
    }
}
