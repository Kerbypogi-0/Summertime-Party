package com.kerbcorp.catchthatpig; // Ensure this matches your package name

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Joystick {
    private int centerX;
    private int centerY;
    private int baseRadius;
    private int hatRadius;
    private int hatX;
    private int hatY;

    // Tracks which finger is pressing this specific joystick (-1 means no touch)
    private int pointerId = -1;

    // Values between -1.0 and 1.0 to tell the player how fast to move
    private float actuatorX = 0.0f;
    private float actuatorY = 0.0f;

    private Paint basePaint;
    private Paint hatPaint;

    public Joystick(int centerX, int centerY, int baseRadius, int hatRadius, int color) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.baseRadius = baseRadius;
        this.hatRadius = hatRadius;
        this.hatX = centerX;
        this.hatY = centerY;

        // The outer circle (semi-transparent white, like your reference image)
        basePaint = new Paint();
        basePaint.setColor(Color.WHITE);
        basePaint.setAlpha(100);
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(10);

        // The inner stick (solid color matching the player)
        hatPaint = new Paint();
        hatPaint.setColor(color);
        hatPaint.setStyle(Paint.Style.FILL);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(hatX, hatY, hatRadius, hatPaint);
    }

    // Checks if a touch event landed inside the joystick base
    public boolean isPressed(double touchX, double touchY) {
        double distance = Math.sqrt(Math.pow(centerX - touchX, 2) + Math.pow(centerY - touchY, 2));
        return distance < baseRadius;
    }

    public void setPointerId(int id) {
        this.pointerId = id;
    }

    public int getPointerId() {
        return pointerId;
    }

    public boolean getIsPressed() {
        return pointerId != -1;
    }

    public void update(double touchX, double touchY) {
        double deltaX = touchX - centerX;
        double deltaY = touchY - centerY;
        double distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        // Keep the inner hat inside the base circle
        if (distance < baseRadius) {
            hatX = (int) touchX;
            hatY = (int) touchY;
        } else {
            hatX = (int) (centerX + (deltaX / distance) * baseRadius);
            hatY = (int) (centerY + (deltaY / distance) * baseRadius);
        }


        // Calculate output for the player (-1.0 to 1.0)
        actuatorX = (hatX - centerX) / (float) baseRadius;
        actuatorY = (hatY - centerY) / (float) baseRadius;
    }

    public void reset() {
        hatX = centerX;
        hatY = centerY;
        actuatorX = 0.0f;
        actuatorY = 0.0f;
        pointerId = -1;
    }

    public float getActuatorX() { return actuatorX; }
    public float getActuatorY() { return actuatorY; }
}