# TexasHoldem-Backend

Spring Boot backend implementing a simplified Texas Hold'em poker engine with REST APIs.

## Running

```
mvn spring-boot:run
```

## User accounts

- `POST /api/users` register a new account
- `POST /api/users/login` login and receive a short-lived token (in-memory)

## API overview
- `POST /api/tables` create a new table (configurable blinds and initial stack)
- `POST /api/tables/{tableId}/players` add human or bot players
  - Human players require a previously registered `userId`
- `POST /api/tables/{tableId}/start` start a hand and deal cards
- `GET /api/tables/{tableId}/state?playerId=...` fetch the current state (hides other hole cards)
- `POST /api/tables/{tableId}/action` submit an action (FOLD, CHECK, CALL, BET, RAISE)
- `POST /api/tables/{tableId}/bots/act` trigger bot decisions
- `POST /api/tables/{tableId}/next-hand` move to the next hand once finished

For a machine-readable description of every endpoint, see `openapi/openapi.yaml`.

## Testing

```
mvn test
```
