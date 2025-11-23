package com.example.poker.controller;

import com.example.poker.domain.GameView;
import com.example.poker.domain.PlayerAction;
import com.example.poker.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables/{tableId}")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public GameView start(@PathVariable String tableId) {
        return gameService.startGame(tableId);
    }

    @GetMapping("/state")
    public GameView state(@PathVariable String tableId, @RequestParam(required = false) String playerId) {
        return gameService.getGameState(tableId, playerId);
    }

    @PostMapping("/action")
    public GameView action(@PathVariable String tableId, @Valid @RequestBody PlayerAction action) {
        return gameService.applyAction(tableId, action);
    }

    @PostMapping("/bots/act")
    public GameView botsAct(@PathVariable String tableId) {
        return gameService.botsAct(tableId);
    }

    @PostMapping("/next-hand")
    public GameView nextHand(@PathVariable String tableId) {
        return gameService.nextHand(tableId);
    }
}
