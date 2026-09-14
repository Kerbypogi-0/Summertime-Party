package com.kerbcorp.matchinggame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Title / home screen. Shows the beach background, the game name, and a
 * Play button that launches MatchingGameActivity.
 *
 * This is the LAUNCHER activity now — see AndroidManifest.xml.
 * UI is defined in res/layout/activity_matching_game.xml.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_game);

        Button playButton = findViewById(R.id.btnPlay);
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MatchingGameActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.btnSettings);
        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance(this).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.getInstance(this).pause();
    }
}