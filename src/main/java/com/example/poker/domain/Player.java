package com.example.poker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {
    private final String id;
    private final String name;
    private final PlayerType type;
    private final String userId;
    private int stack;
    private int currentBet;
    private boolean folded;
    private int seatPosition;
    private final List<Card> holeCards = new ArrayList<>();

    public Player(String name, PlayerType type, String userId, int initialStack) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.userId = userId;
        this.stack = initialStack;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public int getSeatPosition() {
        return seatPosition;
    }

    public void setSeatPosition(int seatPosition) {
        this.seatPosition = seatPosition;
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    @JsonIgnore
    public boolean isAllIn() {
        return stack == 0 && !folded;
    }

    public void resetForNewHand() {
        holeCards.clear();
        currentBet = 0;
        folded = false;
    }
}
