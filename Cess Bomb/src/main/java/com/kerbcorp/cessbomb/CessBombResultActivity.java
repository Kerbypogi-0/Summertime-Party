package com.kerbcorp.cessbomb;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

/**
 * Shows the outcome of a CessBomb match: win/lose banner, score summary,
 * and buttons to replay or return to the SUMMERTIME PARTY main menu.
 */
public class CessBombResultActivity extends AppCompatActivity {

    public static final String EXTRA_WIN = "cessbomb_extra_win";
    public static final String EXTRA_SCORE = "cessbomb_extra_score";
    public static final String EXTRA_DIFFICULTY = "cessbomb_extra_result_difficulty";
    public static final String EXTRA_TIME_REMAINING = "cessbomb_extra_time_remaining";
    public static final String EXTRA_CHALLENGES_COMPLETED = "cessbomb_extra_challenges_completed";
    public static final String EXTRA_TOTAL_CHALLENGES = "cessbomb_extra_total_challenges";
    public static final String EXTRA_BEST_SCORE = "cessbomb_extra_best_score";
    public static final String EXTRA_NEW_BEST = "cessbomb_extra_new_best";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cessbomb_result);

        boolean win = getIntent().getBooleanExtra(EXTRA_WIN, false);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        String difficultyName = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        int timeRemaining = getIntent().getIntExtra(EXTRA_TIME_REMAINING, 0);
        int challengesCompleted = getIntent().getIntExtra(EXTRA_CHALLENGES_COMPLETED, 0);
        int totalChallenges = getIntent().getIntExtra(EXTRA_TOTAL_CHALLENGES, 0);
        int bestScore = getIntent().getIntExtra(EXTRA_BEST_SCORE, 0);
        boolean newBest = getIntent().getBooleanExtra(EXTRA_NEW_BEST, false);

        TextView textResultBanner = findViewById(R.id.textResultBanner);
        TextView textResultSubtitle = findViewById(R.id.textResultSubtitle);
        TextView textFinalScore = findViewById(R.id.textFinalScore);
        TextView textDifficultyUsed = findViewById(R.id.textDifficultyUsed);
        TextView textTimeRemaining = findViewById(R.id.textTimeRemaining);
        TextView textChallengesCompleted = findViewById(R.id.textChallengesCompleted);
        TextView textBestScore = findViewById(R.id.textBestScore);
        ViewGroup confettiContainer = findViewById(R.id.confettiContainer);

        if (win) {
            textResultBanner.setText(R.string.cessbomb_win_banner);
            textResultSubtitle.setText(R.string.cessbomb_win_subtitle);
            spawnConfetti(confettiContainer);
        } else {
            textResultBanner.setText(R.string.cessbomb_lose_banner);
            textResultSubtitle.setText(R.string.cessbomb_lose_subtitle);
        }

        textFinalScore.setText(getString(R.string.cessbomb_score_format, score));
        textDifficultyUsed.setText(difficultyName != null ? difficultyName : Difficulty.NORMAL.name());
        textTimeRemaining.setText(getString(R.string.cessbomb_time_left_format, timeRemaining));
        textChallengesCompleted.setText(getString(R.string.cessbomb_challenge_progress_format,
                challengesCompleted, totalChallenges));
        textBestScore.setText(newBest
                ? getString(R.string.cessbomb_new_best_score_format, bestScore)
                : getString(R.string.cessbomb_best_score_format, bestScore));

        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CessBombGameActivity.class);
            intent.putExtra(CessBombGameActivity.EXTRA_DIFFICULTY,
                    difficultyName != null ? difficultyName : Difficulty.NORMAL.name());
            startActivity(intent);
            finish();
        });

        Button backToPartyButton = findViewById(R.id.backToPartyButtonResult);
        // Clears CessBomb's activity stack (intro + game) and returns to whatever
        // launched CessBomb: the SUMMERTIME PARTY main menu.
        backToPartyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CessBombIntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
        });
    }

    /** Lightweight confetti effect: a handful of emoji falling with rotation. */
    private void spawnConfetti(ViewGroup container) {
        if (container == null) return;
        String[] pieces = {"🎉", "🎊", "✨", "🥥", "🍹"};
        Random random = new Random();

        for (int i = 0; i < 14; i++) {
            TextView piece = new TextView(this);
            piece.setText(pieces[random.nextInt(pieces.length)]);
            piece.setTextSize(20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            container.addView(piece, params);

            piece.setX(random.nextInt(300));
            piece.setY(-50);

            ObjectAnimator fall = ObjectAnimator.ofFloat(piece, "translationY", -50f, 900f);
            fall.setDuration(1500 + random.nextInt(800));
            fall.setStartDelay(random.nextInt(400));

            ObjectAnimator spin = ObjectAnimator.ofFloat(piece, "rotation", 0f, 360f);
            spin.setDuration(fall.getDuration());
            spin.setStartDelay(fall.getStartDelay());

            fall.start();
            spin.start();
        }
    }
}
