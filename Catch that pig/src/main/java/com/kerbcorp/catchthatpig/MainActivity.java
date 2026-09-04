package com.kerbcorp.catchthatpig;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.RadioGroup;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 👇 FORCE FULLSCREEN & HIDE NOTIFICATION BAR 👇
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        // 👆 ---------------------------------------- 👆

        setContentView(R.layout.activity_main);

        android.widget.Button btnStartGame = findViewById(R.id.btnStartGame);
        android.widget.RadioGroup playerSelectGroup = findViewById(R.id.playerSelectGroup);

        btnStartGame.setOnClickListener(v -> {
            int numPlayers = 1;
            int selectedId = playerSelectGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.btn2Player) numPlayers = 2;
            else if (selectedId == R.id.btn3Player) numPlayers = 3;
            else if (selectedId == R.id.btn4Player) numPlayers = 4;

            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int screenWidth = metrics.widthPixels > 0 ? metrics.widthPixels : 1080;
            int screenHeight = metrics.heightPixels > 0 ? metrics.heightPixels : 1920;

            com.crunch.catchthatpig.GameView gameView = new com.crunch.catchthatpig.GameView(this, screenWidth, screenHeight, numPlayers);
            setContentView(gameView);
        });
    }
}