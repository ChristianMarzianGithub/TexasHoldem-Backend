package com.example.poker;

import com.example.poker.domain.User;
import com.example.poker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService();
    }

    @Test
    void registersUniqueUser() {
        User user = userService.register("alice", "secret");
        assertThat(userService.findById(user.getId())).isPresent();
    }

    @Test
    void preventsDuplicateUsernames() {
        userService.register("bob", "secret");
        assertThatThrownBy(() -> userService.register("bob", "other"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void loginGeneratesToken() {
        userService.register("carol", "secret");
        String token = userService.login("carol", "secret");
        assertThat(userService.findByToken(token)).isPresent();
    }

    @Test
    void loginRejectsInvalidCredentials() {
        userService.register("dave", "secret");
        assertThatThrownBy(() -> userService.login("dave", "wrong"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
