package com.kerbcorp.cessbomb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ButtonChallenge extends Challenge {

    public static class Option {
        public final String label; // shown on the button, e.g. "☀️ SUN"
        public final String key;   // identifier used to check the answer

        public Option(String label, String key) {
            this.label = label;
            this.key = key;
        }
    }

    private static final Option[] ALL_OPTIONS = new Option[]{
            new Option("☀️ SUN", "SUN"),
            new Option("🏖️ BEACH", "BEACH"),
            new Option("🍦 ICE", "ICE"),
            new Option("🎉 PARTY", "PARTY"),
            new Option("🥥 COCONUT", "COCONUT"),
            new Option("🩴 SLIPPERS", "SLIPPERS"),
            new Option("🍹 DRINK", "DRINK"),
            new Option("🕶️ SHADES", "SHADES")
    };

    private static final String[] CLUE_KEYS =
            {"SUN", "BEACH", "ICE", "PARTY", "COCONUT", "SLIPPERS", "DRINK", "SHADES"};

    private static final String[] CLUES = {
            "Press the symbol for something bright and hot in the sky.",
            "Press the symbol representing something you can find at the beach.",
            "Press the symbol for a cold summer treat.",
            "Press the symbol that matches the party mood!",
            "Press the symbol for a tropical fruit with a hard shell.",
            "Press the symbol for beach footwear.",
            "Press the symbol for a cool tropical drink.",
            "Press the symbol for something you wear to block the sun."
    };

    public List<Option> options;
    public int correctIndex;

    public static ButtonChallenge generate(Difficulty difficulty, Random random) {
        ButtonChallenge c = new ButtonChallenge();
        c.type = ChallengeType.BUTTON;

        int clueIndex = random.nextInt(CLUE_KEYS.length);
        String targetKey = CLUE_KEYS[clueIndex];
        c.promptText = CLUES[clueIndex];

        Option correctOption = null;
        List<Option> pool = new ArrayList<>();
        for (Option o : ALL_OPTIONS) {
            if (o.key.equals(targetKey)) {
                correctOption = o;
            } else {
                pool.add(o);
            }
        }
        Collections.shuffle(pool, random);

        int optionCount = Math.min(difficulty.optionCount, ALL_OPTIONS.length);
        List<Option> chosen = new ArrayList<>(pool.subList(0, optionCount - 1));
        chosen.add(correctOption);
        Collections.shuffle(chosen, random);

        c.options = chosen;
        c.correctIndex = chosen.indexOf(correctOption);
        return c;
    }

    public boolean checkAnswer(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}
