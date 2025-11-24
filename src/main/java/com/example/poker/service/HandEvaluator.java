package com.example.poker.service;

import com.example.poker.domain.*;

import java.util.*;
import java.util.stream.Collectors;

public final class HandEvaluator {
    private HandEvaluator() {}

    public static HandValue evaluate(List<Card> holeCards, List<Card> communityCards) {
        List<Card> cards = new ArrayList<>(holeCards);
        cards.addAll(communityCards);
        Map<Suit, List<Card>> suits = cards.stream().collect(Collectors.groupingBy(Card::getSuit));
        Map<Integer, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(c -> c.getRank().getValue(), Collectors.counting()));

        List<Integer> ranksDesc = rankCounts.keySet().stream().sorted(Comparator.reverseOrder()).toList();
        Optional<HandValue> straightFlush = straightFlush(suits);
        if (straightFlush.isPresent()) {
            return straightFlush.get();
        }

        Optional<HandValue> quads = ofAKind(rankCounts, 4, ranksDesc);
        if (quads.isPresent()) {
            return quads.get();
        }

        Optional<HandValue> fullHouse = fullHouse(rankCounts, ranksDesc);
        if (fullHouse.isPresent()) {
            return fullHouse.get();
        }

        Optional<HandValue> flush = flush(suits);
        if (flush.isPresent()) {
            return flush.get();
        }

        Optional<HandValue> straight = straight(rankCounts.keySet());
        if (straight.isPresent()) {
            return straight.get();
        }

        Optional<HandValue> trips = ofAKind(rankCounts, 3, ranksDesc);
        if (trips.isPresent()) {
            return trips.get();
        }

        Optional<HandValue> twoPair = twoPair(rankCounts, ranksDesc);
        if (twoPair.isPresent()) {
            return twoPair.get();
        }

        Optional<HandValue> onePair = ofAKind(rankCounts, 2, ranksDesc);
        if (onePair.isPresent()) {
            return onePair.get();
        }

        return new HandValue(HandRankType.HIGH_CARD, topKickers(ranksDesc, 5));
    }

    private static Optional<HandValue> straightFlush(Map<Suit, List<Card>> suits) {
        for (List<Card> suitedCards : suits.values()) {
            if (suitedCards.size() < 5) continue;
            Set<Integer> values = suitedCards.stream().map(c -> c.getRank().getValue()).collect(Collectors.toSet());
            Optional<Integer> high = straightHigh(values);
            if (high.isPresent()) {
                HandRankType type = high.get() == 14 ? HandRankType.ROYAL_FLUSH : HandRankType.STRAIGHT_FLUSH;
                return Optional.of(new HandValue(type, List.of(high.get())));
            }
        }
        return Optional.empty();
    }

    private static Optional<HandValue> flush(Map<Suit, List<Card>> suits) {
        for (List<Card> suited : suits.values()) {
            if (suited.size() >= 5) {
                List<Integer> kickers = suited.stream()
                        .map(c -> c.getRank().getValue())
                        .sorted(Comparator.reverseOrder())
                        .limit(5)
                        .toList();
                return Optional.of(new HandValue(HandRankType.FLUSH, kickers));
            }
        }
        return Optional.empty();
    }

    private static Optional<HandValue> straight(Set<Integer> values) {
        Optional<Integer> high = straightHigh(values);
        return high.map(integer -> new HandValue(HandRankType.STRAIGHT, List.of(integer)));
    }

    private static Optional<Integer> straightHigh(Set<Integer> values) {
        Set<Integer> expanded = new HashSet<>(values);
        if (values.contains(14)) {
            expanded.add(1); // wheel straight
        }
        List<Integer> sorted = expanded.stream().sorted().toList();
        int count = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) == sorted.get(i - 1) + 1) {
                count++;
                if (count >= 5) {
                    return Optional.of(sorted.get(i));
                }
            } else if (!sorted.get(i).equals(sorted.get(i - 1))) {
                count = 1;
            }
        }
        return Optional.empty();
    }

    private static Optional<HandValue> ofAKind(Map<Integer, Long> rankCounts, int target, List<Integer> ranksDesc) {
        for (Integer rank : ranksDesc) {
            if (rankCounts.getOrDefault(rank, 0L) >= target) {
                List<Integer> kickers = new ArrayList<>();
                kickers.add(rank);
                ranksDesc.stream().filter(r -> !r.equals(rank)).limit(5 - 1).forEach(kickers::add);
                HandRankType type = switch (target) {
                    case 4 -> HandRankType.FOUR_OF_A_KIND;
                    case 3 -> HandRankType.THREE_OF_A_KIND;
                    case 2 -> HandRankType.ONE_PAIR;
                    default -> HandRankType.HIGH_CARD;
                };
                return Optional.of(new HandValue(type, kickers));
            }
        }
        return Optional.empty();
    }

    private static Optional<HandValue> fullHouse(Map<Integer, Long> rankCounts, List<Integer> ranksDesc) {
        Integer trips = ranksDesc.stream().filter(r -> rankCounts.getOrDefault(r, 0L) >= 3).findFirst().orElse(null);
        if (trips == null) {
            return Optional.empty();
        }
        Integer pair = ranksDesc.stream()
                .filter(r -> !r.equals(trips) && rankCounts.getOrDefault(r, 0L) >= 2)
                .findFirst()
                .orElse(null);
        if (pair != null) {
            return Optional.of(new HandValue(HandRankType.FULL_HOUSE, List.of(trips, pair)));
        }
        return Optional.empty();
    }

    private static Optional<HandValue> twoPair(Map<Integer, Long> rankCounts, List<Integer> ranksDesc) {
        List<Integer> pairs = ranksDesc.stream().filter(r -> rankCounts.getOrDefault(r, 0L) >= 2).limit(2).toList();
        if (pairs.size() == 2) {
            Integer kicker = ranksDesc.stream().filter(r -> !pairs.contains(r)).findFirst().orElse(0);
            List<Integer> kickers = new ArrayList<>(pairs);
            kickers.add(kicker);
            return Optional.of(new HandValue(HandRankType.TWO_PAIR, kickers));
        }
        return Optional.empty();
    }

    private static List<Integer> topKickers(List<Integer> ranksDesc, int limit) {
        return ranksDesc.stream().limit(limit).toList();
    }
}
