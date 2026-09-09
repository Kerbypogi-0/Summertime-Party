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

        showPlayerSelectScreen();
    }

    private void showPlayerSelectScreen() {

        FrameLayout selectLayout = new FrameLayout(this);

        selectLayout.setBackgroundColor(Color.rgb(50, 180, 220));

        LinearLayout buttonColumn = new LinearLayout(this);

        buttonColumn.setOrientation(LinearLayout.VERTICAL);
        buttonColumn.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);

        title.setText("GO SKI");
        title.setTextSize(50);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 80);

        buttonColumn.addView(title);

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

        LinearLayout buttonLayout = new LinearLayout(this);

        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);

        playerButtons = new Button[numPlayers];

        for (int i = 0; i < numPlayers; i++) {

            final int playerNumber = i + 1;

            Button button = createButton("P" + playerNumber + "\nTAP");

            playerButtons[i] = button;

            buttonLayout.addView(button);

            button.setOnClickListener(v -> {
                gameView.playerTap(playerNumber);
            });
        }

        FrameLayout.LayoutParams buttonParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        140
                );

        buttonParams.gravity = Gravity.BOTTOM;

        mainLayout.addView(buttonLayout, buttonParams);

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