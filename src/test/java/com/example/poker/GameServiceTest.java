package com.example.poker;

import com.example.poker.controller.dto.PlayerRequest;
import com.example.poker.controller.dto.TableRequest;
import com.example.poker.domain.*;
import com.example.poker.service.GameService;
import com.example.poker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        PlayerAction action = new PlayerAction();
        action.setPlayerId(hero.getId());
        action.setAction(PlayerActionType.CALL);
        action.setAmount(20);
        GameView after = gameService.applyAction(table.getId(), action);
        assertThat(after.getState().getPot()).isGreaterThanOrEqualTo(40);
    }
}
