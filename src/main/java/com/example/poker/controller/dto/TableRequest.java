package com.example.poker.controller.dto;

import jakarta.validation.constraints.Min;

public class TableRequest {
    @Min(1)
    private int smallBlind = 10;
    @Min(1)
    private int bigBlind = 20;
    @Min(1)
    private int initialStack = 1000;

    public int getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(int bigBlind) {
        this.bigBlind = bigBlind;
    }

    public int getInitialStack() {
        return initialStack;
    }

    public void setInitialStack(int initialStack) {
        this.initialStack = initialStack;
    }
}
