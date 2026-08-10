package com.kerbcorp.goski;

public class GoSkiPlayer {

    private final int playerNumber;

    private float progress;
    private int tapCount;
    private boolean finished;

    public GoSkiPlayer(int playerNumber) {
        this.playerNumber = playerNumber;
        reset();
    }

    public void tap() {

        if (finished) {
            return;
        }

        tapCount++;

        // Move forward every time the player taps.
        progress += 0.015f;

        // Finish when the jet ski reaches the end.
        if (progress >= 1.0f) {
            progress = 1.0f;
            finished = true;
        }
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public float getProgress() {
        return progress;
    }

    public int getTapCount() {
        return tapCount;
    }

    public boolean isFinished() {
        return finished;
    }

    public void reset() {
        progress = 0f;
        tapCount = 0;
        finished = false;
    }
}