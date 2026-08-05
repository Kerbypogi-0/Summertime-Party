package com.kerbcorp.snakeandapple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private volatile boolean isPlaying;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;

    private final int TILE_SIZE = 60;
    private ArrayList<Point> snake;
    private Point apple;
    private Random random;
    private boolean isGameOver = false;

    public enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.RIGHT;

    public GameView(Context context) { super(context); init(); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true); // Smooths the edges of our rounded shapes
        random = new Random();
        snake = new ArrayList<>();
        apple = new Point();

        snake.add(new Point(TILE_SIZE * 5, TILE_SIZE * 5));
        snake.add(new Point(TILE_SIZE * 4, TILE_SIZE * 5));
        snake.add(new Point(TILE_SIZE * 3, TILE_SIZE * 5));

        apple.set(TILE_SIZE * 10, TILE_SIZE * 10);
    }

    public void setDirection(Direction newDir) {
        if (currentDirection == Direction.UP && newDir == Direction.DOWN) return;
        if (currentDirection == Direction.DOWN && newDir == Direction.UP) return;
        if (currentDirection == Direction.LEFT && newDir == Direction.RIGHT) return;
        if (currentDirection == Direction.RIGHT && newDir == Direction.LEFT) return;
        currentDirection = newDir;
    }

    // New Method for Player B to move the apple 1 tile at a time
    public void moveApple(Direction dir) {
        if (dir == Direction.UP) apple.y -= TILE_SIZE;
        else if (dir == Direction.DOWN) apple.y += TILE_SIZE;
        else if (dir == Direction.LEFT) apple.x -= TILE_SIZE;
        else if (dir == Direction.RIGHT) apple.x += TILE_SIZE;

        // Keep the apple inside the screen bounds
        if (apple.x >= getWidth()) apple.x = 0;
        if (apple.x < 0) apple.x = getWidth() - (getWidth() % TILE_SIZE) - TILE_SIZE;
        if (apple.y >= getHeight()) apple.y = 0;
        if (apple.y < 0) apple.y = getHeight() - (getHeight() % TILE_SIZE) - TILE_SIZE;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        if (getWidth() == 0 || getHeight() == 0 || isGameOver) return; // Stop moving if game over!

        // Move the body
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.get(i).x = snake.get(i - 1).x;
            snake.get(i).y = snake.get(i - 1).y;
        }

        // Move the head
        Point head = snake.get(0);
        if (currentDirection == Direction.RIGHT) head.x += TILE_SIZE;
        else if (currentDirection == Direction.LEFT) head.x -= TILE_SIZE;
        else if (currentDirection == Direction.UP) head.y -= TILE_SIZE;
        else if (currentDirection == Direction.DOWN) head.y += TILE_SIZE;

        // Dynamic screen wrapping
        if (head.x >= getWidth()) head.x = 0;
        if (head.x < 0) head.x = getWidth() - (getWidth() % TILE_SIZE) - TILE_SIZE;
        if (head.y >= getHeight()) head.y = 0;
        if (head.y < 0) head.y = getHeight() - (getHeight() % TILE_SIZE) - TILE_SIZE;

        // --- NEW: Collision Detection (Head vs Body) ---
        for (int i = 1; i < snake.size(); i++) {
            if (head.x == snake.get(i).x && head.y == snake.get(i).y) {
                isGameOver = true;

                // Tell the main screen to show the menu
                ((android.app.Activity) getContext()).runOnUiThread(() -> {
                    android.widget.LinearLayout menu = ((android.app.Activity) getContext()).findViewById(R.id.gameOverMenu);
                    if (menu != null) {
                        menu.setVisibility(android.view.View.VISIBLE);
                    }
                });
            }
        }

        // Detect Eating the Apple
        if (head.x == apple.x && head.y == apple.y) {
            snake.add(new Point(-100, -100));
            apple.set((random.nextInt(getWidth() / TILE_SIZE)) * TILE_SIZE,
                    (random.nextInt(getHeight() / TILE_SIZE)) * TILE_SIZE);
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Changed to pure black for the playground contrast
            canvas.drawColor(Color.BLACK);

            // 1. Draw the Apple (Smooth Red Circle with a Green Stem)
            paint.setColor(Color.parseColor("#FF5252"));
            canvas.drawCircle(apple.x + (TILE_SIZE / 2f), apple.y + (TILE_SIZE / 2f), (TILE_SIZE / 2f) - 4, paint);
            paint.setColor(Color.parseColor("#4CAF50")); // Green stem
            canvas.drawRoundRect(new RectF(apple.x + (TILE_SIZE / 2f) - 3, apple.y + 2,
                    apple.x + (TILE_SIZE / 2f) + 3, apple.y + 15), 5, 5, paint);

            // 2. Draw the Snake
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);

                if (i == 0) {
                    paint.setColor(Color.parseColor("#69F0AE"));
                    canvas.drawRoundRect(new RectF(p.x, p.y, p.x + TILE_SIZE, p.y + TILE_SIZE), 20, 20, paint);

                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(p.x + 18, p.y + 18, 5, paint);
                    canvas.drawCircle(p.x + TILE_SIZE - 18, p.y + 18, 5, paint);
                } else {
                    paint.setColor(Color.parseColor("#00E676"));
                    canvas.drawCircle(p.x + (TILE_SIZE / 2f), p.y + (TILE_SIZE / 2f), (TILE_SIZE / 2f) - 6, paint);
                }
            }

            // --- NEW: Draw the Timeout Text ---
            if (isGameOver) {
                paint.setColor(Color.RED);
                paint.setTextSize(100);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("TIMEOUT!", getWidth() / 2f, getHeight() / 2f, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    public void resetGame() {
        snake.clear();
        snake.add(new Point(TILE_SIZE * 5, TILE_SIZE * 5));
        snake.add(new Point(TILE_SIZE * 4, TILE_SIZE * 5));
        snake.add(new Point(TILE_SIZE * 3, TILE_SIZE * 5));
        currentDirection = Direction.RIGHT;
        apple.set(TILE_SIZE * 10, TILE_SIZE * 10);
        isGameOver = false; // Starts the game loop again!
    }
    private void control() {
        try { Thread.sleep(150); } catch (InterruptedException e) { e.printStackTrace(); }
    }
    public void pause() { isPlaying = false; try { gameThread.join(); } catch (InterruptedException e) { } }
    public void resume() { isPlaying = true; gameThread = new Thread(this); gameThread.start(); }
}