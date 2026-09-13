package com.kerbcorp.cessbomb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WireChallenge extends Challenge {

    public static class WireColor {
        public final String name;
        public final int colorHex; // ARGB int, ready for View.setBackgroundColor()

        public WireColor(String name, int colorHex) {
            this.name = name;
            this.colorHex = colorHex;
        }
    }

    private static final WireColor[] ALL_COLORS = new WireColor[]{
            new WireColor("red", 0xFFE53935),
            new WireColor("yellow", 0xFFFDD835),
            new WireColor("blue", 0xFF1E88E5),
            new WireColor("green", 0xFF43A047),
            new WireColor("orange", 0xFFFB8C00),
            new WireColor("pink", 0xFFEC407A)
    };

    private static final String[] CLUE_TEMPLATES = {
            "Disconnect the wire matching the color of the summer drink (it's %s).",
            "Cut the wire the color of the beach ball's brightest stripe (%s).",
            "Snip the wire that matches the color of fresh mango ice cream (%s).",
            "Disarm the wire matching the color of the sunset (%s)."
    };

    public List<WireColor> wires;
    public int correctIndex;

    public static WireChallenge generate(Difficulty difficulty, Random random) {
        WireChallenge c = new WireChallenge();
        c.type = ChallengeType.WIRE;

        List<WireColor> pool = new ArrayList<>();
        Collections.addAll(pool, ALL_COLORS);
        Collections.shuffle(pool, random);

        int wireCount = Math.min(difficulty.optionCount, ALL_COLORS.length);
        c.wires = new ArrayList<>(pool.subList(0, wireCount));

        c.correctIndex = random.nextInt(c.wires.size());
        String colorName = c.wires.get(c.correctIndex).name;
        String template = CLUE_TEMPLATES[random.nextInt(CLUE_TEMPLATES.length)];
        c.promptText = String.format(template, colorName);

        return c;
    }

    public boolean checkAnswer(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}
