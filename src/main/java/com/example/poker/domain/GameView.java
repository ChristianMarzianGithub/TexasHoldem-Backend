package com.example.poker.domain;

import java.util.List;

public class GameView {
    private final Table table;
    private final GameState state;
    private final List<PlayerView> players;

    public GameView(Table table, GameState state, List<PlayerView> players) {
        this.table = table;
        this.state = state;
        this.players = players;
    }

    public Table getTable() {
        return table;
    }

    public GameState getState() {
        return state;
    }

    public List<PlayerView> getPlayers() {
        return players;
    }
}
