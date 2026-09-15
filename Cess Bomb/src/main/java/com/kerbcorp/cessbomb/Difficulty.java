package com.kerbcorp.cessbomb;


public enum Difficulty {
    EASY(40, 3, 4, 3),
    NORMAL(28, 4, 5, 4),
    HARD(18, 5, 6, 5);

    public final int timeSeconds;
    public final int challengeCount;
    public final int optionCount;
    public final int sequenceLength;
    Difficulty(int timeSeconds, int challengeCount, int optionCount, int sequenceLength) {
        this.timeSeconds = timeSeconds;
        this.challengeCount = challengeCount;
        this.optionCount = optionCount;
        this.sequenceLength = sequenceLength;
    }
}
