package com.example.poker.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Card {
    private final Rank rank;
    private final Suit suit;

    public Card(@JsonProperty("rank") Rank rank, @JsonProperty("suit") Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
