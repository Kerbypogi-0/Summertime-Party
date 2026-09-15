package com.kerbcorp.cessbomb;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MemoryChallenge extends Challenge {

    public static final String[] ICON_POOL = {
            "☀️", "🏖️", "🍦", "🎉", "🥥", "🩴", "🍹", "🕶️"
    };

    public List<String> sequence;

    public static MemoryChallenge generate(Difficulty difficulty, Random random) {
        MemoryChallenge c = new MemoryChallenge();
        c.type = ChallengeType.MEMORY;
        c.promptText = "Memorize the sequence, then tap the icons in the same order!";

        List<String> generated = new ArrayList<>();
        for (int i = 0; i < difficulty.sequenceLength; i++) {
            generated.add(ICON_POOL[random.nextInt(ICON_POOL.length)]);
        }
        c.sequence = generated;

        return c;
    }

    public boolean checkAnswer(List<String> playerInput) {
        return sequence.equals(playerInput);
    }
}