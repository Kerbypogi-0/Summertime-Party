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
            Intent intent = new Intent(MainActivity.this, CatchThatPigActivity.class);
            startActivity(intent);
        });

        Button btnGoSki = findViewById(R.id.btnGoSki);
        btnPig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoSkiActivity.class);
            startActivity(intent);
        });

        Button btnCessBomb = findViewById(R.id.btnCessBomb);
        btnPig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CessBombActivity.class);
            startActivity(intent);
        });

        Button btnMatchingGame = findViewById(R.id.btnMatching);
        btnPig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MatchingGameActivity.class);
            startActivity(intent);
        });
    }
}