package com.kerbcorp.snakeandapple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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

    // Grid and Game objects
    private final int TILE_SIZE = 50;
    private ArrayList<Point> snake;
    private Point apple;
    private Random random;

    // Directions
    public enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.RIGHT;

    // Constructors required to let XML layouts use this custom view
    public GameView(Context context) {
        super(context);
        init();
    }
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        paint = new Paint();
        random = new Random();
        snake = new ArrayList<>();
        apple = new Point();

        // Start the snake with 3 segments
        snake.add(new Point(TILE_SIZE * 5, TILE_SIZE * 5)); // Head
        snake.add(new Point(TILE_SIZE * 4, TILE_SIZE * 5)); // Body 1
        snake.add(new Point(TILE_SIZE * 3, TILE_SIZE * 5)); // Body 2

        spawnApple();
    }

    public void spawnApple() {
        // Randomly pick a grid tile for the new apple
        int gridX = random.nextInt(15) + 2;
        int gridY = random.nextInt(20) + 2;
        apple.set(gridX * TILE_SIZE, gridY * TILE_SIZE);
    }

    public void setDirection(Direction newDir) {
        // Prevent the snake from reversing directly into itself
        if (currentDirection == Direction.UP && newDir == Direction.DOWN) return;
        if (currentDirection == Direction.DOWN && newDir == Direction.UP) return;
        if (currentDirection == Direction.LEFT && newDir == Direction.RIGHT) return;
        if (currentDirection == Direction.RIGHT && newDir == Direction.LEFT) return;
        currentDirection = newDir;
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
        // 1. Move the body (each segment follows the one in front of it)
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.get(i).x = snake.get(i - 1).x;
            snake.get(i).y = snake.get(i - 1).y;
        }

        // 2. Move the head based on the current button pressed
        Point head = snake.get(0);
        if (currentDirection == Direction.RIGHT) head.x += TILE_SIZE;
        else if (currentDirection == Direction.LEFT) head.x -= TILE_SIZE;
        else if (currentDirection == Direction.UP) head.y -= TILE_SIZE;
        else if (currentDirection == Direction.DOWN) head.y += TILE_SIZE;

        // 3. Screen Wrap (If it goes off screen, teleport to the other side)
        if (head.x >= 1200) head.x = 0;
        if (head.x < 0) head.x = 1200;
        if (head.y >= 2000) head.y = 0;
        if (head.y < 0) head.y = 2000;

        // 4. Detect Eating the Apple
        if (head.x == apple.x && head.y == apple.y) {
            // Add a temporary hidden block that will snap into the tail next frame
            snake.add(new Point(-100, -100));
            spawnApple(); // Spawn a new apple somewhere else!
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.parseColor("#121212")); // Dark UI

            // Draw the Apple
            paint.setColor(Color.parseColor("#FF5252"));
            canvas.drawRect(apple.x, apple.y, apple.x + TILE_SIZE, apple.y + TILE_SIZE, paint);

            // Draw the Snake (Loops through every segment in the array list!)
            paint.setColor(Color.parseColor("#69F0AE"));
            for (Point p : snake) {
                canvas.drawRect(p.x, p.y, p.x + TILE_SIZE, p.y + TILE_SIZE, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            // Drop the frame rate to roughly 6 FPS so the grid movement is visible
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        isPlaying = false;
        try { gameThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}