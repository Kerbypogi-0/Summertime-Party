package com.kerbcorp.cessbomb;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Hosts a Cess Bomb play session: difficulty pick -> trivia rounds -> score.
 * All question/round logic lives in GameView; this activity owns the
 * difficulty selector, HUD, and the end-of-game dialog.
 */
public class CessBombActivity extends AppCompatActivity implements GameView.Listener {

    private View hudRow;
    private View difficultySelector;
    private TextView tvStage, tvLives, tvScore, tvQuestion, tvFeedback;
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cess_bomb);

        hudRow = findViewById(R.id.hudRow);
        difficultySelector = findViewById(R.id.difficultySelector);
        tvStage = findViewById(R.id.tvStage);
        tvLives = findViewById(R.id.tvLives);
        tvScore = findViewById(R.id.tvScore);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        gameView = findViewById(R.id.gameView);

        gameView.setListener(this);

        Button btnEasy = findViewById(R.id.btnEasy);
        Button btnMedium = findViewById(R.id.btnMedium);
        Button btnHard = findViewById(R.id.btnHard);

        btnEasy.setOnClickListener(v -> beginGame(GameView.Difficulty.EASY));
        btnMedium.setOnClickListener(v -> beginGame(GameView.Difficulty.MEDIUM));
        btnHard.setOnClickListener(v -> beginGame(GameView.Difficulty.HARD));
    }

    private void beginGame(GameView.Difficulty difficulty) {
        difficultySelector.setVisibility(View.GONE);
        hudRow.setVisibility(View.VISIBLE);
        tvQuestion.setVisibility(View.VISIBLE);
        tvFeedback.setVisibility(View.VISIBLE);
        gameView.setVisibility(View.VISIBLE);

        gameView.post(() -> {
            gameView.startNewGame(difficulty);
            refreshHud();
        });
    }

    private void showDifficultySelector() {
        difficultySelector.setVisibility(View.VISIBLE);
        hudRow.setVisibility(View.GONE);
        tvQuestion.setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);
        gameView.setVisibility(View.GONE);
    }

    private void refreshHud() {
        tvStage.setText("Stage " + gameView.currentStageNumber() + "/" + gameView.totalStages());
        tvLives.setText("Lives: " + gameView.livesRemaining());
        tvScore.setText("Score: " + gameView.currentScore());
        tvQuestion.setText(gameView.currentQuestion());
        tvFeedback.setText("");
    }

    @Override
    public void onAnswer(boolean correct, int livesRemaining, int score) {
        tvLives.setText("Lives: " + livesRemaining);
        tvScore.setText("Score: " + score);
        tvFeedback.setText(correct ? "Nice find!" : "BOOM!");
        tvFeedback.postDelayed(this::refreshHud, correct ? 700 : 600);
    }

    @Override
    public void onGameOver(boolean cleared, int stageReached, int score, int totalStages) {
        String message = (cleared
                ? "You cleared every stage!"
                : "You reached Stage " + stageReached + " of " + totalStages)
                + "\nFinal score: " + score + "/" + totalStages;

        new AlertDialog.Builder(this)
                .setTitle("Game over")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play again", (d, w) -> beginGame(gameView.currentDifficulty()))
                .setNegativeButton("Change difficulty", (d, w) -> showDifficultySelector())
                .setNeutralButton("Back to hub", (d, w) -> finish())
                .show();
    }
}