# TexasHoldem-Backend

Spring Boot backend implementing a simplified Texas Hold'em poker engine with REST APIs, plus a React + Material UI frontend for table control and gameplay.

## Backend

### Running
```bash
mvn spring-boot:run
```

### User accounts
- `POST /api/users` register a new account
- `POST /api/users/login` login and receive a short-lived token (in-memory)

### API overview
- `POST /api/tables` create a new table (configurable blinds and initial stack)
- `POST /api/tables/{tableId}/players` add human or bot players
  - Human players require a previously registered `userId`
- `POST /api/tables/{tableId}/start` start a hand and deal cards
- `GET /api/tables/{tableId}/state?playerId=...` fetch the current state (hides other hole cards)
- `POST /api/tables/{tableId}/action` submit an action (FOLD, CHECK, CALL, BET, RAISE)
- `POST /api/tables/{tableId}/bots/act` trigger bot decisions
- `POST /api/tables/{tableId}/next-hand` move to the next hand once finished

For a machine-readable description of every endpoint, see `openapi/openapi.yaml`.

### Testing
```bash
mvn test
```

## Frontend (React + Vite + Material UI)
Frontend lives in `/frontend` and communicates with the backend API.

### Local development
```bash
cd frontend
npm install
npm start # runs Vite dev server
```
The UI defaults to `http://localhost:8080` for the backend. Override via `VITE_BACKEND_URL` or set it from the landing page.

### Build
```bash
npm run build
```
Outputs production assets to `frontend/dist`.

### Tests
```bash
npm test
```
Runs Vitest with React Testing Library.

### Docker
```bash
cd frontend
docker build -t poker-frontend .
docker run -p 8080:8080 poker-frontend
```
The container serves static assets on port 8080 for Cloud Run compatibility.
