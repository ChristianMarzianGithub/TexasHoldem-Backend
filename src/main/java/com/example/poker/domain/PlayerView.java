package com.example.poker.domain;

import java.util.List;

public class PlayerView {
    private final String id;
    private final String name;
    private final PlayerType type;
    private final String userId;
    private final int stack;
    private final int currentBet;
    private final boolean folded;
    private final int seatPosition;
    private final List<Card> holeCards;

    public PlayerView(Player player, boolean revealCards) {
        this.id = player.getId();
        this.name = player.getName();
        this.type = player.getType();
        this.userId = player.getUserId();
        this.stack = player.getStack();
        this.currentBet = player.getCurrentBet();
        this.folded = player.isFolded();
        this.seatPosition = player.getSeatPosition();
        this.holeCards = revealCards ? List.copyOf(player.getHoleCards()) : List.of();
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

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isFolded() {
        return folded;
    }

    public int getSeatPosition() {
        return seatPosition;
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }
}
