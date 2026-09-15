package com.kerbcorp.catchthatpig;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Pig {
    public float x;
    public float y;
    private float radius = 15f;
    private Paint paint;
    public boolean isCaught = false;

    // --- NEW: Movement Variables ---
    private float speedX;
    private float speedY;
    private static final float PIG_SPEED = 3.5f; // Adjust this to make them faster/slower

    public android.graphics.RectF trappedBounds = null;

    public Pig(float x, float y) {
        this.x = x;
        this.y = y;

        paint = new Paint();
        paint.setColor(Color.rgb(255, 182, 193));
        paint.setStyle(Paint.Style.FILL);

        // Give each pig a random starting direction
        double randomAngle = Math.random() * 2 * Math.PI;
        speedX = (float) (Math.cos(randomAngle) * PIG_SPEED);
        speedY = (float) (Math.sin(randomAngle) * PIG_SPEED);
    }

    // --- NEW: Update method for movement and arena boundaries ---
    public void update(int arenaCenterX, int arenaCenterY, float arenaRadius) {
        x += speedX;
        y += speedY;

        if (isCaught && trappedBounds != null) {
            // Keep the pig bouncing inside the cage boundaries
            int radius = 15; // Replace with your pig's actual visual radius
            if (x < trappedBounds.left + radius) {
                x = trappedBounds.left + radius;
                speedX = Math.abs(speedX);
            }
            if (x > trappedBounds.right - radius) {
                x = trappedBounds.right - radius;
                speedX = -Math.abs(speedX);
            }
            if (y < trappedBounds.top + radius) {
                y = trappedBounds.top + radius;
                speedY = Math.abs(speedY);
            }
            if (y > trappedBounds.bottom - radius) {
                y = trappedBounds.bottom - radius;
                speedY = -Math.abs(speedY);
            }
        } else {

            // Check distance from the center of the arena (Pythagorean theorem)
            double dx = x - arenaCenterX;
            double dy = y - arenaCenterY;
            double distanceFromCenter = Math.sqrt(dx * dx + dy * dy);

            // If the pig hits the edge of the dirt circle (accounting for the pig's own radius)
            if (distanceFromCenter + radius > arenaRadius) {
                // Push it slightly back inside so it doesn't get stuck
                x -= speedX;
                y -= speedY;

                // Calculate the angle pointing directly back at the center
                double angleToCenter = Math.atan2(arenaCenterY - y, arenaCenterX - x);

                // Add a little randomness so they don't just bounce back and forth in a straight line
                angleToCenter += (Math.random() - 0.5);

                // Set the new speed based on the new angle
                speedX = (float) (Math.cos(angleToCenter) * PIG_SPEED);
                speedY = (float) (Math.sin(angleToCenter) * PIG_SPEED);
            }
        }
    }
    public void draw(Canvas canvas) {
        if (paint == null) {
            paint = new Paint();
            paint.setColor(Color.rgb(255, 182, 193));
            paint.setStyle(Paint.Style.FILL);
        }
        canvas.drawCircle(x, y, radius, paint);
    }
}