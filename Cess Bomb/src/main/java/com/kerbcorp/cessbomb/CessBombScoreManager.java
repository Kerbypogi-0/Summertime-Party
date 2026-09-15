package com.kerbcorp.cessbomb;

import android.content.Context;
import android.content.SharedPreferences;


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

    public boolean saveScoreIfBest(int score) {
        int best = getBestScore();
        if (score > best) {
            prefs.edit().putInt(KEY_BEST_SCORE, score).apply();
            return true;
        }
        return false;
    }
}
