package com.kerbcorp.matchinggame;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

/**
 * Plays a single looping background track across the whole app and
 * remembers the chosen volume between sessions (SharedPreferences).
 *
 * Usage:
 *   MusicManager.getInstance(this).start();   // call in onResume()
 *   MusicManager.getInstance(this).pause();   // call in onPause()
 *   MusicManager.getInstance(this).setVolume(0.7f); // 0f..1f, from Settings
 */
public class MusicManager {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_VOLUME = "music_volume";
    private static final float DEFAULT_VOLUME = 0.5f;

    private static MusicManager instance;

    private final Context appContext;
    private MediaPlayer mediaPlayer;
    private float volume;

    private MusicManager(Context context) {
        this.appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        volume = prefs.getFloat(KEY_VOLUME, DEFAULT_VOLUME);
    }

    public static synchronized MusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicManager(context);
        }
        return instance;
    }

    /** Starts (or resumes) the looping track. Safe to call from every activity's onResume(). */
    public void start() {
        if (mediaPlayer == null) {
            // Expects res/raw/bg_music.(wav|mp3|ogg) — see the comment at the
            // bottom of this file for how to add that resource.
            mediaPlayer = MediaPlayer.create(appContext, R.raw.bg_music);
            if (mediaPlayer == null) {
                return; // res/raw/bg_music is missing — silently skip rather than crash
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(volume, volume);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /** Pauses playback without losing position. Call from every activity's onPause(). */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /** newVolume is 0f (silent) to 1f (full volume). Also saved for next launch. */
    public void setVolume(float newVolume) {
        volume = Math.max(0f, Math.min(1f, newVolume));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_VOLUME, volume)
                .apply();
    }

    public float getVolume() {
        return volume;
    }

    /** Fully stops and frees the player. Only needed if you want music to stop for good. */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}