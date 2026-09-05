package com.kerbcorp.catchthatpig;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Cage {
    // RectF holds 4 coordinates (left, top, right, bottom) making it perfect for hitboxes
    private RectF bounds;
    private Paint paint;
    private int score = 0; // We will use this later to track how many pigs are in this cage

    public Cage(float left, float top, float right, float bottom, int color) {
        bounds = new RectF(left, top, right, bottom);

        paint = new Paint();
        paint.setColor(color);
        // Set style to STROKE so it draws an outline (like a fence) instead of a solid box
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20); // Make the fence thick
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(bounds, paint);
    }

    // We will need these later for collision detection and scoring
    public RectF getBounds() { return bounds; }
    public void addScore() { score++; }
    public int getScore() { return score; }
}