package com.kerbcorp.cessbomb;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores CessBomb's best score locally using SharedPreferences so it survives
 * app restarts.
 */
public class CessBombScoreManager {

    private static final String PREFS_NAME = "cessbomb_prefs";
    private static final String KEY_BEST_SCORE = "best_score";

    private final SharedPreferences prefs;

    public CessBombScoreManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getBestScore() {
        return prefs.getInt(KEY_BEST_SCORE, 0);
    }

    /** Saves the score if it beats the current best. Returns true if it's a new best. */
    public boolean saveScoreIfBest(int score) {
        int best = getBestScore();
        if (score > best) {
            prefs.edit().putInt(KEY_BEST_SCORE, score).apply();
            return true;
        }
        return false;
    }
}
