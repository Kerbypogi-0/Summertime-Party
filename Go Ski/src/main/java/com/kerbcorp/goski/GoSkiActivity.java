package com.kerbcorp.goski;

import android.content.pm.ActivityInfo;
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
    private FrameLayout mainLayout;

    private Button[] playerButtons;
    private Button playAgainButton;
    private Button backToMenuButton;
    private LinearLayout postRaceRow;

    private TextView countdownText;

    private int numPlayers = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        showPlayerSelectScreen();
    }

    private void showPlayerSelectScreen() {

        FrameLayout selectLayout = new FrameLayout(this);

        selectLayout.setBackgroundColor(Color.rgb(50, 180, 220));

        LinearLayout buttonColumn = new LinearLayout(this);

        buttonColumn.setOrientation(LinearLayout.VERTICAL);
        buttonColumn.setGravity(Gravity.CENTER);

        android.widget.ImageView logoImage = new android.widget.ImageView(this);

        logoImage.setImageResource(R.drawable.go_ski_logo);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        600,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        logoParams.gravity = Gravity.CENTER;
        logoParams.bottomMargin = 60;

        logoImage.setAdjustViewBounds(true);

        buttonColumn.addView(logoImage, logoParams);

        Button twoPlayerButton = new Button(this);

        twoPlayerButton.setText("2 PLAYERS");
        twoPlayerButton.setTextSize(22);

        Button fourPlayerButton = new Button(this);

        fourPlayerButton.setText("4 PLAYERS");
        fourPlayerButton.setTextSize(22);

        LinearLayout.LayoutParams btnParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        btnParams.setMargins(80, 20, 80, 20);

        buttonColumn.addView(twoPlayerButton, btnParams);
        buttonColumn.addView(fourPlayerButton, btnParams);

        FrameLayout.LayoutParams columnParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        columnParams.gravity = Gravity.CENTER;

        selectLayout.addView(buttonColumn, columnParams);

        setContentView(selectLayout);

        twoPlayerButton.setOnClickListener(v -> {
            numPlayers = 2;
            startGameScreen();
        });

        fourPlayerButton.setOnClickListener(v -> {
            numPlayers = 4;
            startGameScreen();
        });
    }

    private void startGameScreen() {

        mainLayout = new FrameLayout(this);

        gameView = new GameView(this, numPlayers);

        gameView.setOnRaceFinishedListener(winner -> {
            showPostRaceButtons();
        });

        mainLayout.addView(gameView);

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

        mainLayout.addView(countdownText, countdownParams);

        int margin = 30;

        int[] gravities;

        if (numPlayers == 2) {
            gravities = new int[] {
                    Gravity.TOP | Gravity.START,
                    Gravity.BOTTOM | Gravity.END
            };
        } else {
            gravities = new int[] {
                    Gravity.TOP | Gravity.START,
                    Gravity.TOP | Gravity.END,
                    Gravity.BOTTOM | Gravity.START,
                    Gravity.BOTTOM | Gravity.END
            };
        }

        playerButtons = new Button[numPlayers];

        for (int i = 0; i < numPlayers; i++) {

            final int playerNumber = i + 1;

            Button button = createButton("P" + playerNumber + "\nTAP");

            playerButtons[i] = button;

            FrameLayout.LayoutParams cornerParams =
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );

            cornerParams.gravity = gravities[i];
            cornerParams.setMargins(margin, margin, margin, margin);

            mainLayout.addView(button, cornerParams);

            button.setOnClickListener(v -> {
                gameView.playerTap(playerNumber);
            });
        }

        postRaceRow = new LinearLayout(this);

        postRaceRow.setOrientation(LinearLayout.HORIZONTAL);
        postRaceRow.setGravity(Gravity.CENTER);
        postRaceRow.setVisibility(android.view.View.GONE);

        playAgainButton = new Button(this);
        playAgainButton.setText("PLAY AGAIN");
        playAgainButton.setTextSize(20);

        backToMenuButton = new Button(this);
        backToMenuButton.setText("MAIN MENU");
        backToMenuButton.setTextSize(20);

        LinearLayout.LayoutParams postRaceBtnParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        postRaceBtnParams.setMargins(20, 0, 20, 0);

        postRaceRow.addView(playAgainButton, postRaceBtnParams);
        postRaceRow.addView(backToMenuButton, postRaceBtnParams);

        FrameLayout.LayoutParams postRaceRowParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        postRaceRowParams.gravity = Gravity.CENTER;
        postRaceRowParams.topMargin = 250;

        mainLayout.addView(postRaceRow, postRaceRowParams);

        playAgainButton.setOnClickListener(v -> {
            postRaceRow.setVisibility(android.view.View.GONE);
            gameView.resetRace();
            setButtonsEnabled(false);
            startCountdown();
        });

        backToMenuButton.setOnClickListener(v -> {
            postRaceRow.setVisibility(android.view.View.GONE);
            showPlayerSelectScreen();
        });

        setContentView(mainLayout);

        setButtonsEnabled(false);

        startCountdown();
    }

    private void showPostRaceButtons() {

        setButtonsEnabled(false);

        postRaceRow.setVisibility(android.view.View.VISIBLE);
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.BLACK);

        return button;
    }

    private void setButtonsEnabled(boolean enabled) {

        for (Button button : playerButtons) {
            button.setEnabled(enabled);
        }
    }

    private void startCountdown() {

        Handler handler = new Handler();

        countdownText.setText("3");

        handler.postDelayed(() -> {
            countdownText.setText("2");
        }, 1000);

        handler.postDelayed(() -> {
            countdownText.setText("1");
        }, 2000);

        handler.postDelayed(() -> {
            countdownText.setText("GO!");
            gameView.startRace();
            setButtonsEnabled(true);
        }, 3000);

        handler.postDelayed(() -> {
            countdownText.setText("");
        }, 3700);
    }
}