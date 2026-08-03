package com.kerbcorp.snakeandapple; // Ensure this matches your exact package name

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private volatile boolean isPlaying;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;

    // Game Objects Variables
    private int snakeHeadX = 200;
    private int snakeHeadY = 200;
    private int snakeSize = 50;

    private int appleX = 600;
    private int appleY = 400;

    public GameView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        paint = new Paint();
    }

    @Override
    public void run() {
        // The Core Game Loop
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        // Basic movement to test the engine (moves snake right automatically)
        // You will replace this with directional logic (UP, DOWN, LEFT, RIGHT) later!
        snakeHeadX += 5;

        // Wrap around the screen if it goes too far right
        if (snakeHeadX > 1500) {
            snakeHeadX = 0;
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // 1. Draw a sleek, dark UI background
            canvas.drawColor(Color.parseColor("#121212"));

            // 2. Draw the Apple (Vibrant Red)
            paint.setColor(Color.parseColor("#FF5252"));
            canvas.drawRect(appleX, appleY, appleX + snakeSize, appleY + snakeSize, paint);

            // 3. Draw the Snake Head (Neon Green)
            paint.setColor(Color.parseColor("#69F0AE"));
            canvas.drawRect(snakeHeadX, snakeHeadY, snakeHeadX + snakeSize, snakeHeadY + snakeSize, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            // Cap the frame rate at roughly 60 FPS
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // This is where you will catch screen taps or swipes!
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            // For testing: When the screen is tapped, teleport the apple
            appleX = (int) (Math.random() * 800);
            appleY = (int) (Math.random() * 800);
        }
        return true; // Return true to indicate you handled the touch
    }
}