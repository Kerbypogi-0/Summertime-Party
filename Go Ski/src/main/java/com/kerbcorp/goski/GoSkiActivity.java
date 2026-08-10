package com.kerbcorp.goski;

import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GoSkiActivity extends AppCompatActivity {

    private GameView gameView;

    private Button player1Button;
    private Button player2Button;
    private Button player3Button;
    private Button player4Button;

    private TextView countdownText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the main screen
        FrameLayout mainLayout = new FrameLayout(this);

        // Create the game
        gameView = new GameView(this);

        mainLayout.addView(gameView);

        // Countdown text
        countdownText = new TextView(this);

        countdownText.setText("3");
        countdownText.setTextSize(60);
        countdownText.setTextColor(Color.WHITE);
        countdownText.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams countdownParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        mainLayout.addView(
                countdownText,
                countdownParams
        );

        // Bottom button layout
        LinearLayout buttonLayout = new LinearLayout(this);

        buttonLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttonLayout.setGravity(Gravity.CENTER);

        // Player buttons
        player1Button = createButton("P1\nTAP");
        player2Button = createButton("P2\nTAP");
        player3Button = createButton("P3\nTAP");
        player4Button = createButton("P4\nTAP");

        buttonLayout.addView(player1Button);
        buttonLayout.addView(player2Button);
        buttonLayout.addView(player3Button);
        buttonLayout.addView(player4Button);

        FrameLayout.LayoutParams buttonParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        140
                );

        buttonParams.gravity = Gravity.BOTTOM;

        mainLayout.addView(
                buttonLayout,
                buttonParams
        );

        // Set the screen
        setContentView(mainLayout);

        // Disable buttons before GO
        setButtonsEnabled(false);

        // Player 1
        player1Button.setOnClickListener(v -> {
            gameView.playerTap(1);
        });

        // Player 2
        player2Button.setOnClickListener(v -> {
            gameView.playerTap(2);
        });

        // Player 3
        player3Button.setOnClickListener(v -> {
            gameView.playerTap(3);
        });

        // Player 4
        player4Button.setOnClickListener(v -> {
            gameView.playerTap(4);
        });

        // Start countdown
        startCountdown();
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        button.setLayoutParams(params);

        return button;
    }

    private void setButtonsEnabled(boolean enabled) {

        player1Button.setEnabled(enabled);
        player2Button.setEnabled(enabled);
        player3Button.setEnabled(enabled);
        player4Button.setEnabled(enabled);
    }

    private void startCountdown() {

        Handler handler = new Handler();

        // 3
        countdownText.setText("3");

        handler.postDelayed(() -> {

            countdownText.setText("2");

        }, 1000);

        // 1
        handler.postDelayed(() -> {

            countdownText.setText("1");

        }, 2000);

        // GO
        handler.postDelayed(() -> {

            countdownText.setText("GO!");

            gameView.startRace();

            setButtonsEnabled(true);

        }, 3000);

        // Remove GO text
        handler.postDelayed(() -> {

            countdownText.setText("");

        }, 3700);
    }
}