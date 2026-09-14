package com.kerbcorp.matchinggame;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MatchingGameActivity extends AppCompatActivity implements GameView.GameListener {

    /** Converts a dp value to actual screen pixels, so margins look consistent on every device. */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private static final int MAX_MOVES = 8;

    private GameView gameView;
    private TextView statsText;

    private int lastMoves = 0;
    private int lastMatches = 0;
    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setBackgroundResource(R.drawable.bg_beach);

        gameView = new GameView(this);
        gameView.setGameListener(this);
        mainLayout.addView(gameView);

        TextView backButton = new TextView(this);
        backButton.setText(R.string.back_button);
        backButton.setTextSize(16);
        backButton.setTextColor(Color.WHITE);
        backButton.setTypeface(null, Typeface.BOLD);
        backButton.setPadding(24, 24, 24, 0);
        backButton.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        backParams.gravity = Gravity.TOP | Gravity.START;
        backParams.setMargins(dpToPx(16), dpToPx(48), 0, 0);
        mainLayout.addView(backButton, backParams);

        statsText = new TextView(this);
        statsText.setTextSize(18);
        statsText.setTextColor(Color.WHITE);
        statsText.setPadding(24, 24, 24, 0);
        FrameLayout.LayoutParams statsParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        statsParams.gravity = Gravity.TOP | Gravity.START;
        statsParams.topMargin = dpToPx(88);
        mainLayout.addView(statsText, statsParams);

        Button resetButton = new Button(this);
        resetButton.setText(R.string.reset_game);
        resetButton.setTextColor(Color.WHITE);
        resetButton.setBackgroundColor(Color.parseColor("#0F766E"));
        resetButton.setOnClickListener(v -> resetGame());
        FrameLayout.LayoutParams resetParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        resetParams.gravity = Gravity.TOP | Gravity.END;
        resetParams.setMargins(0, dpToPx(48), dpToPx(16), 0);
        mainLayout.addView(resetButton, resetParams);

        setContentView(mainLayout);

        updateStats(0, 0);
    }

    private void resetGame() {
        gameOver = false;
        gameView.setEnabled(true);
        gameView.resetGame();
        updateStats(0, 0);
    }

    @Override
    public void onMove(int moveCount) {
        if (gameOver) {
            return;
        }
        updateStats(moveCount, lastMatches);
        checkGameOver();
    }

    @Override
    public void onMatchFound(int matchedPairs, int totalPairs) {
        if (gameOver) {
            return;
        }
        updateStats(lastMoves, matchedPairs);
        checkGameOver();
    }

    @Override
    public void onGameComplete(int moveCount) {
        gameOver = true;
        statsText.setText(getString(R.string.game_complete, moveCount));
    }

    private void checkGameOver() {
        if (lastMoves >= MAX_MOVES && lastMatches < gameView.getTotalPairs()) {
            gameOver = true;
            gameView.setEnabled(false);
            statsText.setText(getString(R.string.game_over, lastMoves));
        }
    }

    private void updateStats(int moves, int matches) {
        lastMoves = moves;
        lastMatches = matches;
        statsText.setText(getString(R.string.game_stats, lastMoves, lastMatches, gameView.getTotalPairs()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance(this).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.getInstance(this).pause();
    }
}