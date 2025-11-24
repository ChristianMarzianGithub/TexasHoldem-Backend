package com.example.poker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tableId;
    private String heroId;

    @BeforeEach
    void setUp() throws Exception {
        tableId = createTable();
        heroId = registerAndSeatHero();
    }

    @Test
    void startGameRequiresTwoPlayers() throws Exception {
        mockMvc.perform(post(startUrl())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Need at least 2 players to start"));
    }

    @Test
    void startGameReturnsInitialState() throws Exception {
        addBotPlayer("Bot One");

        mockMvc.perform(post(startUrl())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state.phase").value("PRE_FLOP"))
                .andExpect(jsonPath("$.state.pot").value(30))
                .andExpect(jsonPath("$.state.communityCards").isArray());
    }

    @Test
    void botsActStopsWhenHumanTurnReached() throws Exception {
        addBotPlayer("Bot One");
        addBotPlayer("Bot Two");

        mockMvc.perform(post(startUrl())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post(botsUrl())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        int currentIndex = root.path("state").path("currentPlayerIndex").asInt();
        String currentType = root.path("players").get(currentIndex).path("type").asText();
        assertThat(currentType).isEqualTo("HUMAN");
    }

    private String registerAndSeatHero() throws Exception {
        String username = "hero" + UUID.randomUUID();
        String userPayload = "{\"username\":\"" + username + "\",\"password\":\"secret\"}";
        String userId = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        String playerPayload = "{\"name\":\"Hero\",\"type\":\"HUMAN\",\"userId\":\"" + userId + "\"}";
        String response = mockMvc.perform(post(playersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private void addBotPlayer(String name) throws Exception {
        String payload = "{\"name\":\"" + name + "\",\"type\":\"BOT\"}";
        mockMvc.perform(post(playersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    private String createTable() throws Exception {
        String payload = "{\"smallBlind\":10,\"bigBlind\":20,\"initialStack\":1000}";
        String response = mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private String startUrl() {
        return "/api/tables/" + tableId + "/start";
    }

    private String playersUrl() {
        return "/api/tables/" + tableId + "/players";
    }

    private String botsUrl() {
        return "/api/tables/" + tableId + "/bots/act";
    }
}
