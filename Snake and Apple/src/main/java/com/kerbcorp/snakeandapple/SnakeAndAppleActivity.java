package com.kerbcorp.snakeandapple;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SnakeAndAppleActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);

        gameView = findViewById(R.id.gameView);

        // Player A: Snake Controls
        Button btnUp = findViewById(R.id.btnUp);
        Button btnDown = findViewById(R.id.btnDown);
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);

        btnUp.setOnClickListener(v -> gameView.setDirection(GameView.Direction.UP));
        btnDown.setOnClickListener(v -> gameView.setDirection(GameView.Direction.DOWN));
        btnLeft.setOnClickListener(v -> gameView.setDirection(GameView.Direction.LEFT));
        btnRight.setOnClickListener(v -> gameView.setDirection(GameView.Direction.RIGHT));

        // Player B: Apple Controls
        Button btnAppleUp = findViewById(R.id.btnAppleUp);
        Button btnAppleDown = findViewById(R.id.btnAppleDown);
        Button btnAppleLeft = findViewById(R.id.btnAppleLeft);
        Button btnAppleRight = findViewById(R.id.btnAppleRight);

        btnAppleUp.setOnClickListener(v -> gameView.moveApple(GameView.Direction.UP));
        btnAppleDown.setOnClickListener(v -> gameView.moveApple(GameView.Direction.DOWN));
        btnAppleLeft.setOnClickListener(v -> gameView.moveApple(GameView.Direction.LEFT));
        btnAppleRight.setOnClickListener(v -> gameView.moveApple(GameView.Direction.RIGHT));
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