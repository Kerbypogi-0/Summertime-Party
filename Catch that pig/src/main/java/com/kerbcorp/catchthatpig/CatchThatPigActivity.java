package com.kerbcorp.catchthatpig;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.RadioGroup;

public class CatchThatPigActivity extends Activity {

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

        setContentView(R.layout.activity_catch_that_pig_menu);

        Button btnStartGame = findViewById(R.id.btnStartGame);
        RadioGroup playerSelectGroup = findViewById(R.id.playerSelectGroup);

        btnStartGame.setOnClickListener(v -> {
            int numPlayers = 1;
            int selectedId = playerSelectGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.btn2Player) numPlayers = 2;
            else if (selectedId == R.id.btn3Player) numPlayers = 3;
            else if (selectedId == R.id.btn4Player) numPlayers = 4;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int screenWidth = metrics.widthPixels > 0 ? metrics.widthPixels : 1080;
            int screenHeight = metrics.heightPixels > 0 ? metrics.heightPixels : 1920;

            GameView gameView = new GameView(this, screenWidth, screenHeight, numPlayers);
            setContentView(gameView);
        });
    }
}