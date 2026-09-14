package com.kerbcorp.summertimeparty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

// Import the specific game activities your team created
import com.kerbcorp.snakeandapple.SnakeAndAppleActivity;
import com.kerbcorp.catchthatpig.CatchThatPigActivity;
import com.kerbcorp.goski.GoSkiActivity;
import com.kerbcorp.cessbomb.CessBombActivity;
import com.kerbcorp.matchinggame.MatchingGameActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Snake Button
        Button btnSnake = findViewById(R.id.btnSnake);
        btnSnake.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SnakeAndAppleActivity.class);
            startActivity(intent);
        });

        // Setup Pig Button
        Button btnPig = findViewById(R.id.btnPig);
        btnPig.setOnClickListener(v -> {
            // This now correctly jumps to Catch That Pig
            Intent intent = new Intent(MainActivity.this, com.kerbcorp.catchthatpig.CatchThatPigActivity.class);
            startActivity(intent);
        });

        // Setup Go Ski Button
        Button btnGoSki = findViewById(R.id.btnGoSki);
        btnGoSki.setOnClickListener(v -> { // FIXED: Was btnPig
            Intent intent = new Intent(MainActivity.this, GoSkiActivity.class);
            startActivity(intent);
        });

        // Setup Cess Bomb Button
        Button btnCessBomb = findViewById(R.id.btnCessBomb);
        btnCessBomb.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CessBombActivity.class);
            startActivity(intent);
        });

        // Setup Matching Game Button
        Button btnMatchingGame = findViewById(R.id.btnMatching);
        btnMatchingGame.setOnClickListener(v -> { // FIXED: Was btnPig
            Intent intent = new Intent(MainActivity.this, MatchingGameActivity.class);
            startActivity(intent);
        });
    }
}