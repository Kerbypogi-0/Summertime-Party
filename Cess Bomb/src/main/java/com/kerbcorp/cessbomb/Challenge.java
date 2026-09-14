package com.kerbcorp.cessbomb;

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
