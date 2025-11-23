package com.example.poker.controller;

import com.example.poker.controller.dto.PlayerRequest;
import com.example.poker.controller.dto.TableRequest;
import com.example.poker.domain.Player;
import com.example.poker.domain.Table;
import com.example.poker.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final GameService gameService;

    public TableController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Table createTable(@Valid @RequestBody TableRequest request) {
        return gameService.createTable(request);
    }

    @PostMapping("/{tableId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public Player addPlayer(@PathVariable String tableId, @Valid @RequestBody PlayerRequest request) {
        return gameService.addPlayer(tableId, request);
    }

    @GetMapping("/{tableId}")
    public Table getTable(@PathVariable String tableId) {
        return gameService.getTable(tableId);
    }
}
