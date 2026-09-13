package com.kerbcorp.cessbomb;

/**
 * Difficulty levels for CessBomb.
 * Controls timer length, number of challenges, and challenge complexity.
 */
public enum Difficulty {
    EASY(40, 3, 4, 3),
    NORMAL(28, 4, 5, 4),
    HARD(18, 5, 6, 5);

    public final int timeSeconds;      // total countdown length for the whole match
    public final int challengeCount;   // number of challenges before final defusal
    public final int optionCount;      // number of choices in button/wire challenges
    public final int sequenceLength;   // memory challenge sequence length

    Difficulty(int timeSeconds, int challengeCount, int optionCount, int sequenceLength) {
        this.timeSeconds = timeSeconds;
        this.challengeCount = challengeCount;
        this.optionCount = optionCount;
        this.sequenceLength = sequenceLength;
    }
}
