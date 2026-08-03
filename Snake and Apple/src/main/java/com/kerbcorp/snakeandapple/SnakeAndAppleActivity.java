package com.kerbcorp.snakeandapple; // Ensure this matches your exact package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SnakeAndAppleActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the game engine and set it as the main screen
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the game thread when the app is opened
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game thread when the app is minimized
        gameView.pause();
    }
}