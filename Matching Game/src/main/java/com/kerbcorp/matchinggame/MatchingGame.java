package com.kerbcorp.matchinggame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the deck of cards and all game logic: shuffling, flipping,
 * and resolving matches. Has no Android View/UI code in it, so it's
 * easy to unit test on its own.
 */
public class MatchingGame {

    public static class Card {
        public final String symbol;
        public boolean faceUp = false;
        public boolean matched = false;

        public Card(String symbol) {
            this.symbol = symbol;
        }
    }

    // Swap these out (or add more) to re-theme the game.
    private static final String[] SYMBOL_POOL = {
            "\uD83D\uDC20", "\uD83D\uDC1F", "\uD83E\uDD88", "\uD83D\uDC2C",
            "\uD83D\uDC33", "\uD83E\uDD91", "\uD83D\uDC19", "\uD83E\uDD80",
            "\uD83E\uDD9E", "\uD83E\uDD90", "\uD83D\uDC21", "\uD83D\uDC22",
            "\uD83E\uDDAD", "\uD83E\uDEB8", "\uD83D\uDC1A", "\uD83C\uDF0A",
            "\u2693",       "\uD83D\uDC0B", "\uD83E\uDDAA", "\uD83C\uDFA3"
            // tropical fish, fish, shark, dolphin,
            // whale, squid, octopus, crab,
            // lobster, shrimp, pufferfish, turtle,
            // seal, coral, shell, wave,
            // anchor, whale (alt), oyster, fishing pole
    };

    private final int totalPairs;
    private List<Card> cards;

    private int firstFlippedIndex = -1;
    private int secondFlippedIndex = -1;
    private int moveCount = 0;
    private int matchedPairCount = 0;

    public MatchingGame(int totalPairs) {
        this.totalPairs = totalPairs;
        reset();
    }

    public void reset() {
        cards = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            String symbol = SYMBOL_POOL[i % SYMBOL_POOL.length];
            symbols.add(symbol);
            symbols.add(symbol);
        }
        Collections.shuffle(symbols);
        for (String symbol : symbols) {
            cards.add(new Card(symbol));
        }
        firstFlippedIndex = -1;
        secondFlippedIndex = -1;
        moveCount = 0;
        matchedPairCount = 0;
    }

    public List<Card> getCards() {
        return cards;
    }

    /** Returns true if the tap resulted in a flip (i.e. was a legal move). */
    public boolean flipCard(int index) {
        if (index < 0 || index >= cards.size()) return false;
        Card card = cards.get(index);
        if (card.faceUp || card.matched) return false;
        if (isPairPending()) return false;

        card.faceUp = true;

        if (firstFlippedIndex == -1) {
            firstFlippedIndex = index;
        } else {
            secondFlippedIndex = index;
        }
        return true;
    }

    public boolean isPairPending() {
        return firstFlippedIndex != -1 && secondFlippedIndex != -1;
    }

    /** Call after the reveal delay elapses. Returns true if the pair matched. */
    public boolean resolvePendingPair() {
        if (!isPairPending()) return false;

        Card first = cards.get(firstFlippedIndex);
        Card second = cards.get(secondFlippedIndex);
        moveCount++;

        boolean isMatch = first.symbol.equals(second.symbol);
        if (isMatch) {
            first.matched = true;
            second.matched = true;
            matchedPairCount++;
        } else {
            first.faceUp = false;
            second.faceUp = false;
        }

        firstFlippedIndex = -1;
        secondFlippedIndex = -1;
        return isMatch;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getMatchedPairCount() {
        return matchedPairCount;
    }

    public int getTotalPairs() {
        return totalPairs;
    }

    public boolean isComplete() {
        return matchedPairCount == totalPairs;
    }
}