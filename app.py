import json
import random
import uuid
from collections import Counter
from dataclasses import dataclass, field
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from itertools import combinations
from typing import Dict, List, Optional

SUITS = ["♠", "♥", "♦", "♣"]
RANKS = ["2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"]
RANK_VALUES = {r: i + 2 for i, r in enumerate(RANKS)}


@dataclass
class Card:
    rank: str
    suit: str

    def __str__(self) -> str:
        return f"{self.rank}{self.suit}"


class Deck:
    def __init__(self, seed: Optional[int] = None):
        self.cards = [Card(rank, suit) for suit in SUITS for rank in RANKS]
        if seed is not None:
            random.Random(seed).shuffle(self.cards)
        else:
            random.shuffle(self.cards)

    def deal(self, count: int) -> List[Card]:
        return [self.cards.pop() for _ in range(count)]


@dataclass
class PlayerState:
    chips: int = 1000
    bet: int = 0
    hand: List[Card] = field(default_factory=list)
    folded: bool = False

    def reset_for_hand(self):
        self.bet = 0
        self.hand = []
        self.folded = False


@dataclass
class GameState:
    session_id: str
    deck: Deck
    player: PlayerState = field(default_factory=PlayerState)
    bot: PlayerState = field(default_factory=PlayerState)
    community_cards: List[Card] = field(default_factory=list)
    pot: int = 0
    current_bet: int = 0
    stage: str = "preflop"
    message: str = "New hand started."
    hand_over: bool = False
    winner: Optional[str] = None

    def reset_hand(self):
        self.deck = Deck()
        self.community_cards = []
        self.pot = 0
        self.current_bet = 0
        self.stage = "preflop"
        self.message = "New hand started."
        self.hand_over = False
        self.winner = None
        self.player.reset_for_hand()
        self.bot.reset_for_hand()
        self.post_blinds()
        self.deal_hole_cards()

    def post_blinds(self):
        small_blind = 10
        big_blind = 20
        self.player.bet = min(self.player.chips, small_blind)
        self.player.chips -= self.player.bet
        self.bot.bet = min(self.bot.chips, big_blind)
        self.bot.chips -= self.bot.bet
        self.current_bet = self.bot.bet
        self.pot = self.player.bet + self.bot.bet
        self.message = "Player posts small blind, bot posts big blind."

    def deal_hole_cards(self):
        self.player.hand = self.deck.deal(2)
        self.bot.hand = self.deck.deal(2)

    def deal_next_stage(self):
        if self.stage == "preflop":
            self.community_cards.extend(self.deck.deal(3))
            self.stage = "flop"
        elif self.stage == "flop":
            self.community_cards.extend(self.deck.deal(1))
            self.stage = "turn"
        elif self.stage == "turn":
            self.community_cards.extend(self.deck.deal(1))
            self.stage = "river"
        else:
            self.stage = "showdown"
        self.current_bet = 0
        self.player.bet = 0
        self.bot.bet = 0

    def best_hand(self, cards: List[Card]) -> tuple:
        def find_straight(values: List[int]) -> Optional[int]:
            unique = sorted(set(values))
            if 14 in unique:
                unique.insert(0, 1)  # Ace low
            for i in range(len(unique) - 4):
                window = unique[i:i + 5]
                if window[0] + 4 == window[4]:
                    return window[4]
            return None

        def evaluate_five(cards_slice: List[Card]):
            vals = sorted([RANK_VALUES[c.rank] for c in cards_slice], reverse=True)
            suits_slice = [c.suit for c in cards_slice]
            counter = Counter(vals)
            cards_len = len(cards_slice)
            is_flush = len(set(suits_slice)) == 1 and cards_len >= 5
            straight_high = find_straight(vals) if cards_len >= 5 else None
            if is_flush and straight_high:
                return (8, [straight_high])
            if 4 in counter.values():
                quad = max(k for k, v in counter.items() if v == 4)
                kicker = max(k for k, v in counter.items() if v == 1)
                return (7, [quad, kicker])
            if sorted(counter.values(), reverse=True)[:2] == [3, 2]:
                trip = max(k for k, v in counter.items() if v == 3)
                pair = max(k for k, v in counter.items() if v == 2)
                return (6, [trip, pair])
            if is_flush:
                return (5, vals)
            if straight_high:
                return (4, [straight_high])
            if 3 in counter.values():
                trip = max(k for k, v in counter.items() if v == 3)
                kickers = sorted((k for k, v in counter.items() if v == 1), reverse=True)[:2]
                return (3, [trip] + kickers)
            pairs = sorted((k for k, v in counter.items() if v == 2), reverse=True)
            if len(pairs) >= 2:
                kick = max((k for k, v in counter.items() if v == 1), default=0)
                return (2, pairs[:2] + [kick])
            if 2 in counter.values():
                pair = max(k for k, v in counter.items() if v == 2)
                kickers = sorted((k for k, v in counter.items() if v == 1), reverse=True)[:3]
                return (1, [pair] + kickers)
            return (0, vals)

        hand_size = min(5, len(cards))
        best = (-1, [])
        for combo in combinations(cards, hand_size):
            rank = evaluate_five(list(combo))
            if rank > best:
                best = rank
        return best

    def winner_label(self) -> str:
        player_rank = self.best_hand(self.player.hand + self.community_cards)
        bot_rank = self.best_hand(self.bot.hand + self.community_cards)
        if player_rank > bot_rank:
            return "player"
        if bot_rank > player_rank:
            return "bot"
        return "split"

    def settle_pot(self):
        result = self.winner_label()
        if result == "player":
            self.player.chips += self.pot
            self.winner = "Player"
            self.message = "Player wins the hand."
        elif result == "bot":
            self.bot.chips += self.pot
            self.winner = "Bot"
            self.message = "Bot wins the hand."
        else:
            split = self.pot // 2
            self.player.chips += split
            self.bot.chips += self.pot - split
            self.winner = "Split pot"
            self.message = "Hand is a tie. Pot split."
        self.hand_over = True

    def ensure_new_hand_if_needed(self):
        if self.player.chips <= 0 or self.bot.chips <= 0:
            self.message = "Game over. Restart to play again."
            self.hand_over = True

    def to_dict(self) -> Dict:
        return {
            "sessionId": self.session_id,
            "player": {
                "chips": self.player.chips,
                "bet": self.player.bet,
                "hand": [str(c) for c in self.player.hand],
                "folded": self.player.folded,
            },
            "bot": {
                "chips": self.bot.chips,
                "bet": self.bot.bet,
                "hand": ["??" for _ in self.bot.hand],
                "folded": self.bot.folded,
            },
            "community": [str(c) for c in self.community_cards],
            "pot": self.pot,
            "currentBet": self.current_bet,
            "stage": self.stage,
            "message": self.message,
            "handOver": self.hand_over,
            "winner": self.winner,
        }


game_sessions: Dict[str, GameState] = {}


def format_action(actor: str, action: str, amount: int = 0) -> str:
    if action == "fold":
        return f"{actor} folds."
    if action == "check":
        return f"{actor} checks."
    if action == "call":
        return f"{actor} calls {amount}."
    if action == "raise":
        return f"{actor} raises to {amount}."
    return ""


def apply_bet(player: PlayerState, amount: int) -> int:
    contribution = min(amount, player.chips)
    player.chips -= contribution
    player.bet += contribution
    return contribution


def bot_strategy(game: GameState) -> str:
    strength = game.best_hand(game.bot.hand + game.community_cards)
    top_rank = strength[0]
    needed = game.current_bet - game.bot.bet
    if game.bot.chips <= 0:
        return "check" if needed == 0 else "call"
    if game.stage in {"preflop", "flop"} and top_rank >= 2 and game.bot.chips > needed:
        raise_to = game.current_bet + 30
        contribution = apply_bet(game.bot, needed)
        game.pot += contribution
        contribution = apply_bet(game.bot, raise_to - game.bot.bet)
        game.pot += contribution
        game.current_bet = game.bot.bet
        game.message = format_action("Bot", "raise", raise_to)
        return "raise"
    if needed == 0:
        game.message = format_action("Bot", "check")
        return "check"
    contribution = apply_bet(game.bot, needed)
    game.pot += contribution
    game.message = format_action("Bot", "call", game.bot.bet)
    return "call"


def progress_if_ready(game: GameState):
    if game.stage == "showdown":
        game.settle_pot()
        return
    if game.player.bet == game.bot.bet and not game.hand_over:
        if game.stage == "river":
            game.stage = "showdown"
            game.settle_pot()
        else:
            game.deal_next_stage()
            game.message = f"Dealt {game.stage}."


def handle_player_action(game: GameState, action: str, amount: int = 0):
    if game.hand_over:
        game.reset_hand()
        return
    needed = game.current_bet - game.player.bet
    if action == "fold":
        game.bot.chips += game.pot
        game.hand_over = True
        game.winner = "Bot"
        game.message = "Player folds. Bot wins the pot."
        return
    if action == "check":
        if needed != 0:
            raise ValueError("Cannot check when facing a bet.")
        game.message = format_action("Player", "check")
    elif action == "call":
        contribution = apply_bet(game.player, needed)
        game.pot += contribution
        game.message = format_action("Player", "call", game.player.bet)
    elif action == "raise":
        target = max(game.current_bet, game.player.bet) + amount
        contribution = apply_bet(game.player, target - game.player.bet)
        game.pot += contribution
        game.current_bet = game.player.bet
        game.message = format_action("Player", "raise", game.player.bet)
    else:
        raise ValueError("Unknown action")

    if not game.hand_over:
        bot_strategy(game)
        progress_if_ready(game)


def api_new_game() -> Dict:
    session_id = str(uuid.uuid4())
    game = GameState(session_id=session_id, deck=Deck())
    game.reset_hand()
    game_sessions[session_id] = game
    return game.to_dict()


def api_action(session_id: str, action: str, amount: int = 0):
    if session_id not in game_sessions:
        return None, 404, "Session not found"
    game = game_sessions[session_id]
    try:
        handle_player_action(game, action, amount)
    except ValueError as exc:
        return None, 400, str(exc)
    game.ensure_new_hand_if_needed()
    return game.to_dict(), 200, None


class HoldemHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory="static", **kwargs)

    def _send_json(self, payload: Dict, status: int = 200):
        body = json.dumps(payload).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/":
            self.path = "/index.html"
        return super().do_GET()

    def do_POST(self):
        length = int(self.headers.get("Content-Length", "0"))
        body = self.rfile.read(length)
        data = json.loads(body or b"{}")
        if self.path == "/api/new-game":
            response = api_new_game()
            self._send_json(response)
        elif self.path == "/api/action":
            session_id = data.get("sessionId")
            action = data.get("action")
            amount = int(data.get("amount", 0))
            payload, status, error = api_action(session_id, action, amount)
            if error:
                self._send_json({"error": error}, status=status)
            else:
                self._send_json(payload, status=status)
        else:
            self.send_error(404, "Endpoint not found")


def run(host: str = "0.0.0.0", port: int = 8000):
    server = ThreadingHTTPServer((host, port), HoldemHandler)
    print(f"Serving Texas Hold'em on http://{host}:{port}")
    server.serve_forever()


if __name__ == "__main__":
    run()
