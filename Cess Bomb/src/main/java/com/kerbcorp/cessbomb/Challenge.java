package com.kerbcorp.cessbomb;

/**
 * Base class for all CessBomb challenges.
 * Each concrete challenge generates its own randomized data and knows how to check an answer.
 */
public abstract class Challenge {
    protected String promptText;
    protected ChallengeType type;

    public String getPromptText() {
        return promptText;
    }

    public ChallengeType getType() {
        return type;
    }
}
