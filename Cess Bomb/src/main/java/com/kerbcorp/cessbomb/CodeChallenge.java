package com.kerbcorp.cessbomb;

import java.util.Random;

public class CodeChallenge extends Challenge {

    public String equationLine1;
    public String equationLine2;
    public String questionLine;
    public int correctAnswer;

    public static CodeChallenge generate(Difficulty difficulty, Random random) {
        CodeChallenge c = new CodeChallenge();
        c.type = ChallengeType.CODE;

        // Assign small positive values to two summer icons and derive two equations.
        int iceCream = 1 + random.nextInt(5); // 1-5
        int coconut = 1 + random.nextInt(5);  // 1-5

        c.equationLine1 = "🍦 + 🍦 = " + (iceCream * 2);
        c.equationLine2 = "🍦 + 🥥 = " + (iceCream + coconut);
        c.questionLine = "What is 🥥?";
        c.correctAnswer = coconut;

        c.promptText = "Solve the summer code to find the missing value.";
        return c;
    }

    public boolean checkAnswer(int enteredValue) {
        return enteredValue == correctAnswer;
    }
}
