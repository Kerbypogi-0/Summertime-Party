package com.kerbcorp.cessbomb;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ChallengeManager {

    private final List<Challenge> queue = new ArrayList<>();
    private int currentIndex = 0;
    private final Random random = new Random();

    public ChallengeManager(Difficulty difficulty) {
        ChallengeType[] types = ChallengeType.values();
        ChallengeType lastType = null;

        for (int i = 0; i < difficulty.challengeCount; i++) {
            ChallengeType type;
            do {
                type = types[random.nextInt(types.length)];
            } while (type == lastType && difficulty.challengeCount > 1);
            lastType = type;

            Challenge challenge;
            switch (type) {
                case BUTTON:
                    challenge = ButtonChallenge.generate(difficulty, random);
                    break;
                case WIRE:
                    challenge = WireChallenge.generate(difficulty, random);
                    break;
                case CODE:
                    challenge = CodeChallenge.generate(difficulty, random);
                    break;
                case MEMORY:
                default:
                    challenge = MemoryChallenge.generate(difficulty, random);
                    break;
            }
            queue.add(challenge);
        }
    }

    public boolean hasNext() {
        return currentIndex < queue.size();
    }

    public Challenge next() {
        return queue.get(currentIndex++);
    }

    public int getTotalCount() {
        return queue.size();
    }

    public int getCompletedCount() {
        return currentIndex;
    }
}
