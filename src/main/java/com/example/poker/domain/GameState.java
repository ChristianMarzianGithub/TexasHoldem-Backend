package com.example.poker.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameState {
    private final String tableId;
    private GamePhase phase = GamePhase.FINISHED;
    private final List<Card> communityCards = new ArrayList<>();
    private int pot;
    private int currentBet;
    private int currentPlayerIndex;
    private final Map<String, String> handRanks = new HashMap<>();
    private List<String> winners = new ArrayList<>();

    public GameState(String tableId) {
        this.tableId = tableId;
    }

    public String getTableId() {
        return tableId;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Map<String, String> getHandRanks() {
        return handRanks;
    }

    public List<String> getWinners() {
        return winners;
    }

    public void setWinners(List<String> winners) {
        this.winners = winners;
    }
}
