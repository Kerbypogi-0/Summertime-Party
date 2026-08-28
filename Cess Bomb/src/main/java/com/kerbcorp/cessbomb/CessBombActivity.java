package com.kerbcorp.cessbomb;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Hosts a single Cess Bomb play session. Launched by the Summertime
 * Party hub app (see the manifest entry noted in AndroidManifest.xml).
 * All scene/question data and animation logic live in GameView; this
 * activity only owns the HUD and the end-of-game dialog.
 */
public class CessBombActivity extends AppCompatActivity implements GameView.Listener {

    private TextView tvStage;
    private TextView tvLives;
    private TextView tvQuestion;
    private TextView tvFeedback;
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cess_bomb);

        tvStage = findViewById(R.id.tvStage);
        tvLives = findViewById(R.id.tvLives);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        gameView = findViewById(R.id.gameView);

        gameView.setListener(this);
        gameView.post(this::startGame);
    }

    private void startGame() {
        gameView.startNewGame();
        refreshHud();
    }

    private void refreshHud() {
        tvStage.setText("Stage " + gameView.currentStageNumber());
        tvLives.setText("Lives: " + gameView.livesRemaining());
        tvQuestion.setText(gameView.currentQuestion());
        tvFeedback.setText("");
    }

    @Override
    public void onAnswer(boolean correct, int livesRemaining) {
        tvLives.setText("Lives: " + livesRemaining);
        tvFeedback.setText(correct ? "Nice find!" : "BOOM!");
        // GameView advances or reloads internally after its short delay;
        // refresh the rest of the HUD once that settles.
        tvFeedback.postDelayed(this::refreshHud, correct ? 700 : 600);
    }

    @Override
    public void onGameOver(boolean cleared, int stageReached) {
        String message = cleared
                ? "You cleared every stage! Summer champion!"
                : "You reached Stage " + stageReached;

        new AlertDialog.Builder(this)
                .setTitle("Game over")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play again", (dialog, which) -> startGame())
                .setNegativeButton("Back to hub", (dialog, which) -> finish())
                .show();
    }
}