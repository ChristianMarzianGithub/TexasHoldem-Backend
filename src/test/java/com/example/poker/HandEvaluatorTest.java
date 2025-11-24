package com.example.poker;

import com.example.poker.domain.*;
import com.example.poker.service.HandEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HandEvaluatorTest {

    @Test
    void detectsStraightFlush() {
        List<Card> hole = List.of(new Card(Rank.ACE, Suit.SPADES), new Card(Rank.KING, Suit.SPADES));
        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS)
        );
        HandValue value = HandEvaluator.evaluate(hole, community);
        assertThat(value.getRankType()).isEqualTo(HandRankType.ROYAL_FLUSH);
    }

    @Test
    void detectsFullHouseOverFlush() {
        List<Card> hole = List.of(new Card(Rank.THREE, Suit.HEARTS), new Card(Rank.THREE, Suit.SPADES));
        List<Card> community = List.of(
                new Card(Rank.THREE, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS)
        );
        HandValue value = HandEvaluator.evaluate(hole, community);
        assertThat(value.getRankType()).isEqualTo(HandRankType.FULL_HOUSE);
        assertThat(value.getKickers()).containsExactly(3, 7);
    }

    @Test
    void detectsTwoPairWithKicker() {
        List<Card> hole = List.of(new Card(Rank.TWO, Suit.CLUBS), new Card(Rank.ACE, Suit.SPADES));
        List<Card> community = List.of(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.DIAMONDS),
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS)
        );
        HandValue value = HandEvaluator.evaluate(hole, community);
        assertThat(value.getRankType()).isEqualTo(HandRankType.TWO_PAIR);
        assertThat(value.getKickers().get(2)).isEqualTo(Rank.KING.getValue());
    }
}
