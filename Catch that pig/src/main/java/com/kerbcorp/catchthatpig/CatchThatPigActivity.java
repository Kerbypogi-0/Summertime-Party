package com.kerbcorp.catchthatpig;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.RadioGroup;

public class CatchThatPigActivity extends androidx.appcompat.app.AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Strip away the default Action Bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // 1. MODERN FULLSCREEN LOGIC (Fixes the first 7 warnings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Fallback for older devices running Android 10 or below
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        // Load your black-accented XML layout
        setContentView(R.layout.activity_catch_that_pig_menu);

        Button btnStartGame = findViewById(R.id.btnStartGame);
        RadioGroup playerSelectGroup = findViewById(R.id.playerSelectGroup);

        btnStartGame.setOnClickListener(v -> {
            int numPlayers = 1;
            int selectedId = playerSelectGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.btn2Player) numPlayers = 2;
            else if (selectedId == R.id.btn3Player) numPlayers = 3;
            else if (selectedId == R.id.btn4Player) numPlayers = 4;

            int screenWidth = 1080;
            int screenHeight = 1920;

            // 2. MODERN SCREEN METRICS LOGIC (Fixes the last 2 warnings)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
                screenWidth = metrics.getBounds().width();
                screenHeight = metrics.getBounds().height();
            }

            GameView gameView = new GameView(this, screenWidth, screenHeight, numPlayers);
            setContentView(gameView);
        });
    }
}