package com.example.poker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tableId;

    @BeforeEach
    void setupTable() throws Exception {
        String payload = objectMapper.writeValueAsString(new TableRequestBuilder()
                .smallBlind(10)
                .bigBlind(20)
                .initialStack(1000));

        tableId = mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void createTableWithoutBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTableWithInvalidBlindsReturnsBadRequest() throws Exception {
        String payload = objectMapper.writeValueAsString(new TableRequestBuilder()
                .smallBlind(0)
                .bigBlind(20)
                .initialStack(1000));

        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTableSucceedsWithValidPayload() throws Exception {
        String payload = objectMapper.writeValueAsString(new TableRequestBuilder()
                .smallBlind(10)
                .bigBlind(20)
                .initialStack(1000));

        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.smallBlind").value(10))
                .andExpect(jsonPath("$.bigBlind").value(20))
                .andExpect(jsonPath("$.initialStack").value(1000));
    }

    @Test
    void addPlayerRequiresName() throws Exception {
        String payload = "{\"type\":\"BOT\"}";

        mockMvc.perform(post(getPlayersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBotPlayerSucceeds() throws Exception {
        String payload = objectMapper.writeValueAsString(new PlayerRequestBuilder()
                .name("Bot One")
                .type("BOT"));

        mockMvc.perform(post(getPlayersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bot One"))
                .andExpect(jsonPath("$.type").value("BOT"))
                .andExpect(jsonPath("$.stack").value(1000));
    }

    @Test
    void addHumanPlayerRequiresUser() throws Exception {
        String payload = objectMapper.writeValueAsString(new PlayerRequestBuilder()
                .name("Hero")
                .type("HUMAN"));

        mockMvc.perform(post(getPlayersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addHumanPlayerUnknownUserReturnsNotFound() throws Exception {
        String payload = objectMapper.writeValueAsString(new PlayerRequestBuilder()
                .name("Hero")
                .type("HUMAN")
                .userId("missing"));

        mockMvc.perform(post(getPlayersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void addHumanPlayerSucceedsWhenUserExists() throws Exception {
        String userPayload = "{\"username\":\"hero\",\"password\":\"secret\"}";
        String userId = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        String payload = objectMapper.writeValueAsString(new PlayerRequestBuilder()
                .name("Hero")
                .type("HUMAN")
                .userId(userId));

        mockMvc.perform(post(getPlayersUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hero"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.stack").value(1000));
    }

    private String getPlayersUrl() {
        String id = tableId.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        return "/api/tables/" + id + "/players";
    }

    private static class TableRequestBuilder {
        private int smallBlind;
        private int bigBlind;
        private int initialStack;

        public TableRequestBuilder smallBlind(int value) {
            this.smallBlind = value;
            return this;
        }

        public TableRequestBuilder bigBlind(int value) {
            this.bigBlind = value;
            return this;
        }

        public TableRequestBuilder initialStack(int value) {
            this.initialStack = value;
            return this;
        }

        public int getSmallBlind() {
            return smallBlind;
        }

        public int getBigBlind() {
            return bigBlind;
        }

        public int getInitialStack() {
            return initialStack;
        }
    }

    private static class PlayerRequestBuilder {
        private String name;
        private String type;
        private String userId;

        public PlayerRequestBuilder name(String value) {
            this.name = value;
            return this;
        }

        public PlayerRequestBuilder type(String value) {
            this.type = value;
            return this;
        }

        public PlayerRequestBuilder userId(String value) {
            this.userId = value;
            return this;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getUserId() {
            return userId;
        }
    }
}
