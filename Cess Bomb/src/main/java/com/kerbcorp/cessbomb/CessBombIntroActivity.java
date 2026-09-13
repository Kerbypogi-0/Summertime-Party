package com.kerbcorp.cessbomb;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry point for the CessBomb mini-game.
 * Launch this Activity from the SUMMERTIME PARTY main menu.
 */
public class CessBombIntroActivity extends AppCompatActivity {

    private Difficulty selectedDifficulty = Difficulty.NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cessbomb_intro);

        TextView bestScoreText = findViewById(R.id.textBestScore);
        int best = new CessBombScoreManager(this).getBestScore();
        bestScoreText.setText(getString(R.string.cessbomb_best_score_format, best));

        RadioGroup difficultyGroup = findViewById(R.id.difficultyRadioGroup);
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEasy) {
                selectedDifficulty = Difficulty.EASY;
            } else if (checkedId == R.id.radioHard) {
                selectedDifficulty = Difficulty.HARD;
            } else {
                selectedDifficulty = Difficulty.NORMAL;
            }
        });

        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(CessBombIntroActivity.this, CessBombGameActivity.class);
            intent.putExtra(CessBombGameActivity.EXTRA_DIFFICULTY, selectedDifficulty.name());
            startActivity(intent);
        });

        Button backButton = findViewById(R.id.backToPartyButton);
        // Simply finishes this Activity, returning to whatever launched CessBomb
        // (the SUMMERTIME PARTY main menu).
        backButton.setOnClickListener(v -> finish());
    }
}
