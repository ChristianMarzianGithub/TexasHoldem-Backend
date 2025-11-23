package com.example.poker.service;

import com.example.poker.controller.dto.PlayerRequest;
import com.example.poker.controller.dto.TableRequest;
import com.example.poker.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
public class GameService {

    private final Map<String, Table> tables = new ConcurrentHashMap<>();
    private final Map<String, GameState> gameStates = new ConcurrentHashMap<>();
    private final UserService userService;

    public GameService(UserService userService) {
        this.userService = userService;
    }

    public synchronized Table createTable(TableRequest request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Table configuration is required");
        }
        if (request.getSmallBlind() < 1 || request.getBigBlind() < 1 || request.getInitialStack() < 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Blinds and stacks must be positive");
        }
        if (request.getBigBlind() <= request.getSmallBlind()) {
            throw new ResponseStatusException(BAD_REQUEST, "Big blind must exceed small blind");
        }
        if (request.getInitialStack() < request.getBigBlind() * 2) {
            throw new ResponseStatusException(BAD_REQUEST, "Initial stack must at least cover blinds");
        }
        Table table = new Table(request.getSmallBlind(), request.getBigBlind(), request.getInitialStack());
        tables.put(table.getId(), table);
        gameStates.put(table.getId(), new GameState(table.getId()));
        return table;
    }

    public synchronized Player addPlayer(String tableId, PlayerRequest request) {
        Table table = getTableOrThrow(tableId);
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Player request is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Player name is required");
        }
        if (request.getType() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Player type is required");
        }
        if (table.getPlayers().size() >= Table.MAX_PLAYERS) {
            throw new ResponseStatusException(CONFLICT, "Table is full");
        }
        String userId = request.getUserId();
        if (request.getType() == PlayerType.HUMAN) {
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(BAD_REQUEST, "Human players require a userId");
            }
            userService.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        }
        Player player = new Player(request.getName(), request.getType(), userId, table.getInitialStack());
        try {
            table.addPlayer(player);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(CONFLICT, ex.getMessage());
        }
        return player;
    }

    public synchronized Table getTable(String tableId) {
        return getTableOrThrow(tableId);
    }

    public synchronized GameView startGame(String tableId) {
        Table table = getTableOrThrow(tableId);
        GameState state = getStateOrThrow(tableId);
        if (table.getPlayers().size() < 2) {
            throw new ResponseStatusException(CONFLICT, "Need at least 2 players to start");
        }
        state.getCommunityCards().clear();
        state.getHandRanks().clear();
        state.setWinners(new ArrayList<>());
        state.setPot(0);
        state.setCurrentBet(0);
        table.resetDeck();
        table.getPlayers().forEach(Player::resetForNewHand);

        dealHoleCards(table);
        applyBlinds(table, state);
        state.setPhase(GamePhase.PRE_FLOP);
        return maskedView(table, state, null);
    }

    public synchronized GameView getGameState(String tableId, String playerId) {
        Table table = getTableOrThrow(tableId);
        GameState state = getStateOrThrow(tableId);
        return maskedView(table, state, playerId);
    }

    public synchronized GameView applyAction(String tableId, PlayerAction action) {
        Table table = getTableOrThrow(tableId);
        GameState state = getStateOrThrow(tableId);
        validateAction(table, state, action);
        Player player = table.findPlayer(action.getPlayerId()).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Player not found"));

        switch (action.getAction()) {
            case FOLD -> handleFold(player, state);
            case CHECK -> handleCheck(player, state);
            case CALL -> handleCall(player, state);
            case BET -> handleBet(player, state, action.getAmount(), table);
            case RAISE -> handleRaise(player, state, action.getAmount(), table);
        }

        if (isHandOver(table, state)) {
            finishHand(table, state);
        } else if (isBettingRoundComplete(table, state)) {
            advancePhase(table, state);
        } else {
            moveToNextPlayer(table, state);
        }

        return maskedView(table, state, action.getPlayerId());
    }

    public synchronized GameView botsAct(String tableId) {
        Table table = getTableOrThrow(tableId);
        GameState state = getStateOrThrow(tableId);
        while (!isHandOver(table, state)) {
            Player current = table.getPlayers().get(state.getCurrentPlayerIndex());
            if (current.getType() == PlayerType.BOT && !current.isFolded()) {
                botMove(table, state, current);
                if (isHandOver(table, state)) {
                    finishHand(table, state);
                    break;
                }
                if (isBettingRoundComplete(table, state)) {
                    advancePhase(table, state);
                } else {
                    moveToNextPlayer(table, state);
                }
            } else {
                break;
            }
        }
        return maskedView(table, state, null);
    }

    public synchronized GameView nextHand(String tableId) {
        Table table = getTableOrThrow(tableId);
        GameState state = getStateOrThrow(tableId);
        if (state.getPhase() != GamePhase.FINISHED && state.getPhase() != GamePhase.SHOWDOWN) {
            throw new ResponseStatusException(CONFLICT, "Current hand not finished");
        }
        table.rotateDealer();
        return startGame(tableId);
    }

    private Table getTableOrThrow(String tableId) {
        Table table = tables.get(tableId);
        if (table == null) {
            throw new ResponseStatusException(NOT_FOUND, "Table not found");
        }
        return table;
    }

    private GameState getStateOrThrow(String tableId) {
        GameState state = gameStates.get(tableId);
        if (state == null) {
            throw new ResponseStatusException(NOT_FOUND, "Game state not found");
        }
        return state;
    }

    private void dealHoleCards(Table table) {
        for (int i = 0; i < 2; i++) {
            for (Player player : table.getPlayers()) {
                player.getHoleCards().add(table.getDeck().draw());
            }
        }
    }

    private void applyBlinds(Table table, GameState state) {
        int smallBlindIndex = nextActivePlayer(table, table.getDealerPosition());
        int bigBlindIndex = nextActivePlayer(table, smallBlindIndex);
        postBlind(table.getPlayers().get(smallBlindIndex), table.getSmallBlind(), state);
        postBlind(table.getPlayers().get(bigBlindIndex), table.getBigBlind(), state);
        state.setCurrentPlayerIndex(nextActivePlayer(table, bigBlindIndex));
        state.setCurrentBet(table.getBigBlind());
    }

    private void postBlind(Player player, int amount, GameState state) {
        int post = Math.min(amount, player.getStack());
        player.setStack(player.getStack() - post);
        player.setCurrentBet(post);
        state.setPot(state.getPot() + post);
    }

    private void validateAction(Table table, GameState state, PlayerAction action) {
        if (state.getPhase() == GamePhase.FINISHED) {
            throw new ResponseStatusException(CONFLICT, "No active hand");
        }
        Player current = table.getPlayers().get(state.getCurrentPlayerIndex());
        if (!current.getId().equals(action.getPlayerId())) {
            throw new ResponseStatusException(CONFLICT, "Not player's turn");
        }
        if (current.isFolded() || current.isAllIn()) {
            throw new ResponseStatusException(CONFLICT, "Player cannot act");
        }
        if (action.getAction() == PlayerActionType.CHECK && current.getCurrentBet() != state.getCurrentBet()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot check when bet outstanding");
        }
        if ((action.getAction() == PlayerActionType.BET || action.getAction() == PlayerActionType.RAISE) && action.getAmount() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Bet/Raise amount must be positive");
        }
    }

    private void handleFold(Player player, GameState state) {
        player.setFolded(true);
    }

    private void handleCheck(Player player, GameState state) {
        // Nothing to do, bet already matched
    }

    private void handleCall(Player player, GameState state) {
        int toCall = state.getCurrentBet() - player.getCurrentBet();
        int callAmount = Math.min(toCall, player.getStack());
        player.setStack(player.getStack() - callAmount);
        player.setCurrentBet(player.getCurrentBet() + callAmount);
        state.setPot(state.getPot() + callAmount);
    }

    private void handleBet(Player player, GameState state, int amount, Table table) {
        if (state.getCurrentBet() > 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot bet, use raise");
        }
        int bet = Math.min(amount, player.getStack());
        if (bet < table.getBigBlind()) {
            throw new ResponseStatusException(BAD_REQUEST, "Bet must be at least big blind");
        }
        applyWager(player, state, bet);
        state.setCurrentBet(bet);
    }

    private void handleRaise(Player player, GameState state, int amount, Table table) {
        if (state.getCurrentBet() == 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Nothing to raise");
        }
        int raiseTotal = Math.min(amount + state.getCurrentBet() - player.getCurrentBet(), player.getStack() + player.getCurrentBet());
        int additional = raiseTotal - player.getCurrentBet();
        if (additional < table.getBigBlind()) {
            throw new ResponseStatusException(BAD_REQUEST, "Raise must be at least big blind");
        }
        applyWager(player, state, additional);
        state.setCurrentBet(player.getCurrentBet());
    }

    private void applyWager(Player player, GameState state, int amount) {
        int wager = Math.min(amount, player.getStack());
        player.setStack(player.getStack() - wager);
        player.setCurrentBet(player.getCurrentBet() + wager);
        state.setPot(state.getPot() + wager);
    }

    private boolean isBettingRoundComplete(Table table, GameState state) {
        int activePlayers = (int) table.getPlayers().stream().filter(p -> !p.isFolded()).count();
        if (activePlayers <= 1) {
            return true;
        }
        return table.getPlayers().stream()
                .filter(p -> !p.isFolded())
                .allMatch(p -> p.getCurrentBet() == state.getCurrentBet() || p.isAllIn());
    }

    private void advancePhase(Table table, GameState state) {
        table.getPlayers().forEach(p -> p.setCurrentBet(0));
        state.setCurrentBet(0);
        switch (state.getPhase()) {
            case PRE_FLOP -> {
                dealCommunityCards(state, table, 3);
                state.setPhase(GamePhase.FLOP);
            }
            case FLOP -> {
                dealCommunityCards(state, table, 1);
                state.setPhase(GamePhase.TURN);
            }
            case TURN -> {
                dealCommunityCards(state, table, 1);
                state.setPhase(GamePhase.RIVER);
            }
            case RIVER -> state.setPhase(GamePhase.SHOWDOWN);
            default -> {
            }
        }
        if (state.getPhase() == GamePhase.SHOWDOWN) {
            finishHand(table, state);
        } else {
            state.setCurrentPlayerIndex(nextActivePlayer(table, table.getDealerPosition()));
        }
    }

    private void dealCommunityCards(GameState state, Table table, int count) {
        for (int i = 0; i < count; i++) {
            state.getCommunityCards().add(table.getDeck().draw());
        }
    }

    private void moveToNextPlayer(Table table, GameState state) {
        state.setCurrentPlayerIndex(nextActivePlayer(table, state.getCurrentPlayerIndex()));
    }

    private int nextActivePlayer(Table table, int startIndex) {
        int index = (startIndex + 1) % table.getPlayers().size();
        for (int i = 0; i < table.getPlayers().size(); i++) {
            Player candidate = table.getPlayers().get(index);
            if (!candidate.isFolded() && candidate.getStack() >= 0) {
                return index;
            }
            index = (index + 1) % table.getPlayers().size();
        }
        return startIndex;
    }

    private boolean isHandOver(Table table, GameState state) {
        long active = table.getPlayers().stream().filter(p -> !p.isFolded()).count();
        return active <= 1 || state.getPhase() == GamePhase.SHOWDOWN;
    }

    private void finishHand(Table table, GameState state) {
        List<Player> contenders = table.getPlayers().stream().filter(p -> !p.isFolded()).collect(Collectors.toList());
        Map<Player, HandValue> rankings = new HashMap<>();
        for (Player player : contenders) {
            HandValue value = HandEvaluator.evaluate(player.getHoleCards(), state.getCommunityCards());
            rankings.put(player, value);
            state.getHandRanks().put(player.getId(), value.toString());
        }
        HandValue best = rankings.values().stream().max(Comparator.naturalOrder()).orElse(null);
        List<Player> winners = rankings.entrySet().stream()
                .filter(e -> e.getValue().compareTo(best) == 0)
                .map(Map.Entry::getKey)
                .toList();
        int share = state.getPot() / winners.size();
        for (Player winner : winners) {
            winner.setStack(winner.getStack() + share);
        }
        state.setWinners(winners.stream().map(Player::getId).toList());
        state.setPhase(GamePhase.FINISHED);
    }

    private GameView maskedView(Table table, GameState state, String playerId) {
        List<PlayerView> masked = table.getPlayers().stream()
                .map(p -> new PlayerView(p, Objects.equals(p.getId(), playerId)))
                .collect(Collectors.toList());
        return new GameView(table, state, masked);
    }

    private void botMove(Table table, GameState state, Player bot) {
        if (bot.getCurrentBet() < state.getCurrentBet()) {
            handleCall(bot, state);
        } else if (state.getCurrentBet() == 0 && bot.getStack() > table.getBigBlind() && new Random().nextBoolean()) {
            handleBet(bot, state, table.getBigBlind(), table);
        } else {
            handleCheck(bot, state);
        }
    }
}
