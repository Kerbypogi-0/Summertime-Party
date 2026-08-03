package com.kerbcorp.snakeandapple;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SnakeAndAppleActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the new layout with the D-Pad buttons
        setContentView(R.layout.activity_snake);

        // Find the engine and the buttons
        gameView = findViewById(R.id.gameView);
        Button btnUp = findViewById(R.id.btnUp);
        Button btnDown = findViewById(R.id.btnDown);
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);

        // Tell the engine to change direction when a button is clicked
        btnUp.setOnClickListener(v -> gameView.setDirection(GameView.Direction.UP));
        btnDown.setOnClickListener(v -> gameView.setDirection(GameView.Direction.DOWN));
        btnLeft.setOnClickListener(v -> gameView.setDirection(GameView.Direction.LEFT));
        btnRight.setOnClickListener(v -> gameView.setDirection(GameView.Direction.RIGHT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}