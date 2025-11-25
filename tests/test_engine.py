import pytest

from app import Card, Deck, GameState


def build_cards(cards):
    return [Card(rank, suit) for rank, suit in cards]


def test_best_hand_prefers_straight_flush_over_quads():
    game = GameState(session_id="t1", deck=Deck(seed=1))
    straight_flush = build_cards([
        ("9", "♠"),
        ("T", "♠"),
        ("J", "♠"),
        ("Q", "♠"),
        ("K", "♠"),
        ("2", "♦"),
        ("3", "♣"),
    ])
    four_of_a_kind = build_cards([
        ("9", "♠"),
        ("9", "♥"),
        ("9", "♦"),
        ("9", "♣"),
        ("K", "♦"),
        ("A", "♠"),
        ("3", "♣"),
    ])
    assert game.best_hand(straight_flush) > game.best_hand(four_of_a_kind)


def test_winner_handles_split_pot():
    game = GameState(session_id="t2", deck=Deck(seed=2))
    # Player: pair of aces with king kicker
    game.player.hand = build_cards([("A", "♠"), ("K", "♦")])
    # Bot: pair of aces with queen kicker
    game.bot.hand = build_cards([("A", "♥"), ("Q", "♣")])
    game.community_cards = build_cards([("A", "♦"), ("5", "♣"), ("7", "♠"), ("9", "♦"), ("2", "♥")])
    assert game.winner_label() == "player"
    game.bot.hand = build_cards([("A", "♥"), ("K", "♣")])
    assert game.winner_label() == "split"
