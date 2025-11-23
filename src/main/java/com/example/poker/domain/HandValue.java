package com.example.poker.domain;

import java.util.List;

public class HandValue implements Comparable<HandValue> {
    private final HandRankType rankType;
    private final List<Integer> kickers;

    public HandValue(HandRankType rankType, List<Integer> kickers) {
        this.rankType = rankType;
        this.kickers = kickers;
    }

    public HandRankType getRankType() {
        return rankType;
    }

    public List<Integer> getKickers() {
        return kickers;
    }

    @Override
    public int compareTo(HandValue other) {
        int rankCompare = Integer.compare(this.rankType.ordinal(), other.rankType.ordinal());
        if (rankCompare != 0) {
            return rankCompare;
        }
        for (int i = 0; i < Math.min(this.kickers.size(), other.kickers.size()); i++) {
            int cmp = Integer.compare(this.kickers.get(i), other.kickers.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(this.kickers.size(), other.kickers.size());
    }

    @Override
    public String toString() {
        return rankType + " " + kickers;
    }
}
