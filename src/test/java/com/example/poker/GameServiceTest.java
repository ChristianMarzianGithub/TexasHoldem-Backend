package com.example.poker;

import com.example.poker.controller.dto.PlayerRequest;
import com.example.poker.controller.dto.TableRequest;
import com.example.poker.domain.*;
import com.example.poker.service.GameService;
import com.example.poker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private Table table;
    private Player hero;

    @BeforeEach
    void setup() {
        userService = new UserService();
        gameService = new GameService(userService);
        TableRequest request = new TableRequest();
        request.setSmallBlind(10);
        request.setBigBlind(20);
        request.setInitialStack(1000);
        table = gameService.createTable(request);

        String heroUser = userService.register("hero", "password").getId();
        PlayerRequest heroReq = new PlayerRequest();
        heroReq.setName("Hero");
        heroReq.setType(PlayerType.HUMAN);
        heroReq.setUserId(heroUser);
        hero = gameService.addPlayer(table.getId(), heroReq);

        PlayerRequest botReq = new PlayerRequest();
        botReq.setName("Bot");
        botReq.setType(PlayerType.BOT);
        gameService.addPlayer(table.getId(), botReq);
    }

    @Test
    void startsGameWithBlindsPosted() {
        GameView view = gameService.startGame(table.getId());
        assertThat(view.getState().getPot()).isEqualTo(30);
        assertThat(view.getState().getPhase()).isEqualTo(GamePhase.PRE_FLOP);
    }

    @Test
    void playerCanCallAndAdvance() {
        gameService.startGame(table.getId());
        GameState state = gameService.getGameState(table.getId(), hero.getId()).getState();
        if (!table.getPlayers().get(state.getCurrentPlayerIndex()).getId().equals(hero.getId())) {
            gameService.botsAct(table.getId());
        }
        PlayerAction action = new PlayerAction();
        action.setPlayerId(hero.getId());
        action.setAction(PlayerActionType.CALL);
        action.setAmount(20);
        GameView after = gameService.applyAction(table.getId(), action);
        assertThat(after.getState().getPot()).isGreaterThanOrEqualTo(40);
    }

    @Test
    void botsStopWhenHumanTurnReached() {
        gameService.startGame(table.getId());
        gameService.botsAct(table.getId());
        GameState state = gameService.getGameState(table.getId(), hero.getId()).getState();
        assertThat(state.getPhase()).isNotEqualTo(GamePhase.FINISHED);
        Player current = table.getPlayers().get(state.getCurrentPlayerIndex());
        assertThat(current.getType()).isEqualTo(PlayerType.HUMAN);
    }

    @Test
    void rejectsMissingTableRequest() {
        assertThatThrownBy(() -> gameService.createTable(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Table configuration is required");
    }

    @Test
    void rejectsInvalidBlindConfiguration() {
        TableRequest invalid = new TableRequest();
        invalid.setSmallBlind(20);
        invalid.setBigBlind(20);
        invalid.setInitialStack(1000);

        assertThatThrownBy(() -> gameService.createTable(invalid))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Big blind must exceed small blind");
    }

    @Test
    void rejectsStackBelowBlinds() {
        TableRequest invalid = new TableRequest();
        invalid.setSmallBlind(10);
        invalid.setBigBlind(20);
        invalid.setInitialStack(10);

        assertThatThrownBy(() -> gameService.createTable(invalid))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Initial stack must at least cover blinds");
    }
}
