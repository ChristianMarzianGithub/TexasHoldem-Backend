package com.example.poker;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}
