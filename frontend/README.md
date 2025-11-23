# Texas Hold'em Frontend

A React + Material UI frontend for the Texas Hold'em backend API. The app lets you create/join a table, play hands, and trigger bot actions while watching the game state update automatically.

## Features
- Landing page to configure backend URL, create a new table, or join an existing one
- Game table layout with player seats, community cards, pot, and current turn indicator
- Action panel for fold/check/call/bet/raise flows and bot triggering after your turn
- Polling-based game state updates every 2 seconds
- TypeScript + React Router v6 + Material UI

## Prerequisites
- Node.js 18+ (tested with Node 22)
- Backend URL (defaults to `http://localhost:8080` or `VITE_BACKEND_URL`)

## Local development
```bash
cd frontend
npm install
npm start # alias for npm run dev
```
The dev server runs on http://localhost:5173 by default. Update the backend URL from the landing page or set `VITE_BACKEND_URL`.

## Build
```bash
npm run build
```
Compiled assets are emitted to `dist/`.

## Tests
```bash
npm test
```
Runs Vitest with jsdom and React Testing Library.

## Docker
Build the production image:
```bash
docker build -t poker-frontend .
```
Run locally:
```bash
docker run -p 8080:8080 poker-frontend
```
The container serves the static build via Nginx on port 8080.

## Deploy to Cloud Run
1. Build and push the image:
   ```bash
   gcloud builds submit --tag gcr.io/PROJECT_ID/poker-frontend
   ```
2. Deploy:
   ```bash
   gcloud run deploy poker-frontend \
     --image gcr.io/PROJECT_ID/poker-frontend \
     --platform managed \
     --allow-unauthenticated \
     --port 8080
   ```

Set the backend URL via the `VITE_BACKEND_URL` build-time variable or configure it from the landing page at runtime.
