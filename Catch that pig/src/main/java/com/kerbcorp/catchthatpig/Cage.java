package com.kerbcorp.catchthatpig;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Cage {
    // RectF holds 4 coordinates (left, top, right, bottom) making it perfect for hitboxes
    private RectF bounds;
    private Bitmap sprite;
    private int score = 0; // Tracks how many pigs are in this cage

    public Cage(float left, float top, float right, float bottom, Bitmap sprite) {
        bounds = new RectF(left, top, right, bottom);
        this.sprite = sprite;
    }

    public void draw(Canvas canvas) {
        if (sprite != null) {
            canvas.drawBitmap(sprite, bounds.left, bounds.top, null);
        }
    }

    // We will need these later for collision detection and scoring
    public RectF getBounds() { return bounds; }
    public void addScore() { score++; }
    public int getScore() { return score; }
}