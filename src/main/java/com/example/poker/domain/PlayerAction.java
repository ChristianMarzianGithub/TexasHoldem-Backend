package com.example.poker.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlayerAction {
    @NotBlank
    private String playerId;
    @NotNull
    private PlayerActionType action;
    @Min(0)
    private int amount;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public PlayerActionType getAction() {
        return action;
    }

    public void setAction(PlayerActionType action) {
        this.action = action;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
