# Texas Hold'em Bot Web App

Play heads-up Texas Hold'em in the browser against a lightweight bot served by a built-in Python HTTP server.

## Getting started

1. Run the development server:

```bash
python app.py
```

The app serves the client at `http://localhost:8000`. Click **New Game** to begin, then choose **Check**, **Call**, **Fold**, or enter a raise amount and click **Raise**.

## Testing

Run the automated tests with pytest:

```bash
pytest
```

## Project structure

- `app.py` – HTTP server, game state, bot logic, and hand evaluation.
- `static/` – Client assets (`index.html`, `style.css`, `app.js`).
- `tests/` – Pytest suites for the game engine and API endpoints.
