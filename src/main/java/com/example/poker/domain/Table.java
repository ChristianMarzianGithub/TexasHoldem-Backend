package com.example.poker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Table {
    public static final int MAX_PLAYERS = 5;
    private final String id;
    private final List<Player> players = new ArrayList<>();
    private int dealerPosition;
    private final int smallBlind;
    private final int bigBlind;
    private final int initialStack;
    @JsonIgnore
    private Deck deck;

    public Table(int smallBlind, int bigBlind, int initialStack) {
        this.id = UUID.randomUUID().toString();
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.initialStack = initialStack;
        this.dealerPosition = 0;
        this.deck = new Deck();
    }

    public String getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getDealerPosition() {
        return dealerPosition;
    }

    public void rotateDealer() {
        dealerPosition = (dealerPosition + 1) % players.size();
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public Deck getDeck() {
        return deck;
    }

    public int getInitialStack() {
        return initialStack;
    }

    public void resetDeck() {
        this.deck = new Deck();
    }

    public Optional<Player> findPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst();
    }

    public void addPlayer(Player player) {
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Table is full");
        }
        player.setSeatPosition(players.size());
        players.add(player);
    }
}
