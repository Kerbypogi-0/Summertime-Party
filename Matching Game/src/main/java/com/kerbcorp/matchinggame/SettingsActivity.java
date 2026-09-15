package com.kerbcorp.matchinggame;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Simple settings screen: one slider controlling music volume.
 * The value is read from / saved to MusicManager, which persists it
 * across app launches.
 */
public class SettingsActivity extends AppCompatActivity {

    /** Converts a dp value to actual screen pixels, so margins look consistent on every device. */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setBackgroundResource(R.drawable.bg_beach);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 0, 64, 0);

        TextView title = new TextView(this);
        title.setText("Settings");
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        TextView label = new TextView(this);
        label.setTextSize(16);
        label.setTextColor(Color.WHITE);
        label.setGravity(Gravity.CENTER);
        root.addView(label);

        SeekBar volumeSeekBar = new SeekBar(this);
        volumeSeekBar.setMax(100);
        MusicManager musicManager = MusicManager.getInstance(this);
        int startingProgress = Math.round(musicManager.getVolume() * 100);
        volumeSeekBar.setProgress(startingProgress);
        label.setText("Music Volume: " + startingProgress + "%");

        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        seekParams.topMargin = 16;
        root.addView(volumeSeekBar, seekParams);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText("Music Volume: " + progress + "%");
                if (fromUser) {
                    musicManager.setVolume(progress / 100f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no-op
            }
        });

        FrameLayout.LayoutParams rootParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mainLayout.addView(root, rootParams);

        TextView backButton = new TextView(this);
        backButton.setText("← Back");
        backButton.setTextSize(16);
        backButton.setTextColor(Color.WHITE);
        backButton.setTypeface(null, Typeface.BOLD);
        backButton.setPadding(24, 24, 24, 0);
        backButton.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        backParams.gravity = Gravity.TOP | Gravity.START;
        backParams.setMargins(dpToPx(16), dpToPx(48), 0, 0);
        mainLayout.addView(backButton, backParams);

        setContentView(mainLayout);
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