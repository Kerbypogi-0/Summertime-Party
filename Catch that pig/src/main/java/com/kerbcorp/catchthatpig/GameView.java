package com.kerbcorp.catchthatpig;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;

public class GameView extends SurfaceView implements Runnable {

    // Core Game Loop Variables
    private Thread thread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;

    // Game Objects
    private Player player1, player2, player3, player4;
    private Joystick joystick1, joystick2, joystick3, joystick4;
    private Cage cage1, cage2, cage3, cage4;
    private List<Pig> pigs = new ArrayList<>();

    // Arena Dimensions
    private int screenWidth;
    private int screenHeight;
    private int arenaCenterX;
    private int arenaCenterY;
    private float arenaRadius;

    // UI & End Game Variables
    private Paint uiBgPaint;
    private Paint textPaint;
    private long startTime;
    private int timeLeft = 60;
    private boolean isGameOver = false;
    private String gameOverTitle = "TIME'S UP!";
    private String winnerText = "";
    private int winnerColor = Color.WHITE;

    private boolean isPaused = false;
    private float pauseLeft, pauseTop, pauseRight, pauseBottom;
    private float btnResumeTop, btnRestartTop, btnMenuTop;
    private float btnWidth = 360;
    private float btnHeight = 90;
    private boolean isCountdown = false;
    private long countdownStartTime;
    private int countdownValue;

    private long gameOverStartTime;
    private int storedNumPlayers;


    public GameView(Context context, int screenWidth, int screenHeight, int numPlayers) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        holder = getHolder();
        paint = new Paint();

        this.storedNumPlayers = numPlayers;

        // 1. UI Initialization
        uiBgPaint = new Paint();
        uiBgPaint.setColor(Color.BLACK);
        uiBgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(45);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        startTime = System.currentTimeMillis();

        // 2. Cages
        int cageSize = 340;
        int shiftX = 125;

        cage4 = new Cage(shiftX, 0, shiftX + cageSize, cageSize, Color.YELLOW);
        cage3 = new Cage(screenWidth - shiftX - cageSize, 0, screenWidth - shiftX, cageSize, Color.GREEN);

        cage1 = new Cage(shiftX, screenHeight - cageSize, shiftX + cageSize, screenHeight, Color.RED);
        cage2 = new Cage(screenWidth - shiftX - cageSize, screenHeight - cageSize, screenWidth - shiftX, screenHeight, Color.BLUE);
        // 3. Multiplayer AI Logic
        boolean p1_AI = false;                  // Red is ALWAYS human
        boolean p3_AI = (numPlayers < 2);       // Green is Human if 2+ players
        boolean p2_AI = (numPlayers < 3);       // Blue is Human if 3+ players
        boolean p4_AI = (numPlayers < 4);       // Yellow is Human if 4 players

        // 4. Joysticks
        int joyBase = 150;
        int joyHat = 70;
        int marginX = 160;
        int marginY = 160;

        if (!p1_AI) joystick1 = new Joystick(marginX, screenHeight - marginY, joyBase, joyHat, Color.RED);
        if (!p2_AI) joystick2 = new Joystick(screenWidth - marginX, screenHeight - marginY, joyBase, joyHat, Color.BLUE);
        if (!p3_AI) joystick3 = new Joystick(screenWidth - marginX, marginY, joyBase, joyHat, Color.GREEN);
        if (!p4_AI) joystick4 = new Joystick(marginX, marginY, joyBase, joyHat, Color.YELLOW);

        // 5. Players
        int cx = screenWidth / 2;
        int cy = screenHeight / 2;
        player1 = new Player(cx - 100, cy + 100, 50, Color.RED, p1_AI, cage1);
        player2 = new Player(cx + 100, cy + 100, 50, Color.BLUE, p2_AI, cage2);
        player3 = new Player(cx + 100, cy - 100, 50, Color.GREEN, p3_AI, cage3);
        player4 = new Player(cx - 100, cy - 100, 50, Color.YELLOW, p4_AI, cage4);

        // 6. Spawn 30 Pigs
        arenaCenterX = screenWidth / 2;
        arenaCenterY = screenHeight / 2;
        arenaRadius = (screenHeight / 2f) - 50;

        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.sqrt(Math.random()) * (arenaRadius - 20);
            float pigX = (float) (arenaCenterX + r * Math.cos(angle));
            float pigY = (float) (arenaCenterY + r * Math.sin(angle));
            pigs.add(new Pig(pigX, pigY));
        }

        // 7. Start Loop Thread
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            controlFPS();
        }
    }

    private void update() {
        if (isPaused) return;

        if (isGameOver) {
            long elapsed = System.currentTimeMillis() - gameOverStartTime;
            if (elapsed >= 5000) { // 5 seconds have passed

                isPlaying = false; // 👈 1. Stop the background game loop immediately

                // Switch to the Main UI Thread to safely change screens
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(intent);
                        ((Activity) getContext()).finish();

                        // 👈 2. Add a built-in Android fade animation for a smooth visual transition
                        ((Activity) getContext()).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });

            }
            return; // Stop updating the game while Game Over is active
        }

        if (isCountdown) {
            long elapsed = System.currentTimeMillis() - countdownStartTime;
            countdownValue = 3 - (int) (elapsed / 1000);
            if (countdownValue <= 0) {
                isCountdown = false;
                startTime = System.currentTimeMillis() - ((60 - timeLeft) * 1000L);
            }
            return;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        timeLeft = 60 - (int) (elapsedTime / 1000);

        int totalCaughtPigs = cage1.getScore() + cage2.getScore() + cage3.getScore() + cage4.getScore();

        // Trigger Game Over and start the 5-second timer
        if (timeLeft <= 0 || totalCaughtPigs >= pigs.size()) {
            timeLeft = Math.max(0, timeLeft);
            isGameOver = true;
            gameOverTitle = (totalCaughtPigs >= pigs.size()) ? "ALL PIGS CAUGHT!" : "TIME'S UP!";
            determineWinner();
            gameOverStartTime = System.currentTimeMillis(); // Record exactly when the game ended
            return;
        }

        // ... (Keep the rest of your pig and player update logic here)

        // 4. Normal Game Updates
        for (Pig pig : pigs) pig.update(arenaCenterX, arenaCenterY, arenaRadius);

        player1.update(joystick1, screenWidth, screenHeight, pigs);
        player2.update(joystick2, screenWidth, screenHeight, pigs);
        player3.update(joystick3, screenWidth, screenHeight, pigs);
        player4.update(joystick4, screenWidth, screenHeight, pigs);

        Player[] players = {player1, player2, player3, player4};
        Cage[] cages = {cage1, cage2, cage3, cage4};

        for (Player p : players) {

            // 1. Pick up a pig
            if (!p.isCarryingPig()) {
                for (Pig pig : pigs) {
                    if (!pig.isCaught && checkPigCollision(p, pig)) {
                        pig.isCaught = true;
                        p.setCarryingPig(true);
                        break;
                    }
                }
            }

            // 2. Drop the pig into a cage
            if (p.isCarryingPig()) {
                for (Cage cage : cages) {
                    if (checkCageCollision(p, cage)) {
                        p.setCarryingPig(false);
                        cage.addScore();

                        // --- TRAP THE PIG IN THE CAGE ---
                        for (Pig pig : pigs) {
                            if (pig.isCaught && pig.trappedBounds == null) {
                                // Safely get the cage boundaries
                                // Safely get the cage boundaries as RectF
                                android.graphics.RectF bounds = cage.getBounds();
                                pig.trappedBounds = new android.graphics.RectF(bounds.left, bounds.top, bounds.right, bounds.bottom);

                                // Snap the pig instantly into the center of the cage
                                pig.x = bounds.left + (bounds.right - bounds.left) / 2f;
                                pig.y = bounds.top + (bounds.bottom - bounds.top) / 2f;
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // 3. Make the pig visually follow the player while being carried
            if (p.isCarryingPig()) {
                for (Pig pig : pigs) {
                    // Find a pig that is caught but not yet dropped in a cage
                    if (pig.isCaught && pig.trappedBounds == null) {
                        pig.x = (float) p.getX();
                        pig.y = (float) p.getY() - 30; // Float slightly above the player
                        break;
                    }
                }
            }
        }
    }

    // Helper method to find the winner when game ends
    private void determineWinner() {
        int s1 = cage1.getScore(); // Red
        int s2 = cage2.getScore(); // Blue
        int s3 = cage3.getScore(); // Green
        int s4 = cage4.getScore(); // Yellow

        int maxScore = Math.max(Math.max(s1, s2), Math.max(s3, s4));

        if (maxScore == 0) {
            winnerText = "NOBODY WON!";
            winnerColor = Color.WHITE;
            return;
        }

        List<String> winners = new ArrayList<>();
        if (s1 == maxScore) winners.add("RED");
        if (s2 == maxScore) winners.add("BLUE");
        if (s3 == maxScore) winners.add("GREEN");
        if (s4 == maxScore) winners.add("YELLOW");

        if (winners.size() == 1) {
            winnerText = winners.get(0) + " WINS (" + maxScore + " pigs)!";
            if (s1 == maxScore) winnerColor = Color.RED;
            else if (s2 == maxScore) winnerColor = Color.BLUE;
            else if (s3 == maxScore) winnerColor = Color.GREEN;
            else if (s4 == maxScore) winnerColor = Color.YELLOW;
        } else {
            winnerText = "TIE GAME (" + maxScore + " pigs)!";
            winnerColor = Color.WHITE;
        }
    }

    private boolean checkPigCollision(Player player, Pig pig) {
        double dx = player.getX() - pig.x;
        double dy = player.getY() - pig.y;
        return Math.hypot(dx, dy) < (player.getRadius() + 15);
    }

    private boolean checkCageCollision(Player player, Cage cage) {
        return cage.getBounds().contains((int) player.getX(), (int) player.getY());
    }

    private void controlFPS() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        pigs.clear();
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.sqrt(Math.random()) * (arenaRadius - 20);
            pigs.add(new Pig((float) (arenaCenterX + r * Math.cos(angle)), (float) (arenaCenterY + r * Math.sin(angle))));
        }

        // Locking the size and shift exactly as requested
        int cageSize = 340;
        int shiftX = 280;
        int shiftX2  = 125;

        cage4 = new Cage(shiftX2, 0, shiftX2 + cageSize, cageSize, Color.YELLOW);
        cage3 = new Cage(screenWidth - shiftX2 - cageSize, 0, screenWidth - shiftX2, cageSize, Color.GREEN);
        cage1 = new Cage(shiftX2, screenHeight - cageSize, shiftX2 + cageSize, screenHeight, Color.RED);
        cage2 = new Cage(screenWidth - shiftX2 - cageSize, screenHeight - cageSize, screenWidth - shiftX2, screenHeight, Color.BLUE);

        // Check how many players were selected to turn the rest into AI
        boolean p1_AI = false;
        boolean p3_AI = (storedNumPlayers < 2);
        boolean p2_AI = (storedNumPlayers < 3);
        boolean p4_AI = (storedNumPlayers < 4);

        player1 = new Player(arenaCenterX - 100, arenaCenterY + 100, 50, Color.RED, p1_AI, cage1);
        player2 = new Player(arenaCenterX + 100, arenaCenterY + 100, 50, Color.BLUE, p2_AI, cage2);
        player3 = new Player(arenaCenterX + 100, arenaCenterY - 100, 50, Color.GREEN, p3_AI, cage3);
        player4 = new Player(arenaCenterX - 100, arenaCenterY - 100, 50, Color.YELLOW, p4_AI, cage4);

        timeLeft = 60;
        startTime = System.currentTimeMillis();
        isGameOver = false;
        isPaused = false;
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();

            // 1. Draw Background First
            canvas.drawColor(Color.rgb(124, 252, 0));

            // 2. Draw Cages Second (So they sit behind the arena)
            cage1.draw(canvas);
            cage2.draw(canvas);
            cage3.draw(canvas);
            cage4.draw(canvas);

            // 3. Draw Arena Third (So it overlaps on top of the cages)
            paint.setColor(Color.rgb(160, 82, 45));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(arenaCenterX, arenaCenterY, arenaRadius, paint);

            // 4. Draw Pigs, Players
            for (Pig pig : pigs) pig.draw(canvas);

            player1.draw(canvas);
            player2.draw(canvas);
            player3.draw(canvas);
            player4.draw(canvas);

            // Draw Joysticks
            if (joystick1 != null) joystick1.draw(canvas);
            if (joystick2 != null) joystick2.draw(canvas);
            if (joystick3 != null) joystick3.draw(canvas);
            if (joystick4 != null) joystick4.draw(canvas);

            // Clustered Scoreboard UI
            float cx = screenWidth / 2f;
            float boxH = 70;
            float timerW = 140;
            float scoreW = 110;
            float gap = 5;

            // TOP CLUSTER
            drawHudBox(canvas, cx - timerW/2 - gap - scoreW, 0, cx - timerW/2 - gap, boxH, String.valueOf(cage4.getScore()), Color.YELLOW);
            drawHudBox(canvas, cx - timerW/2, 0, cx + timerW/2, boxH, "⏳ " + timeLeft, Color.WHITE);
            drawHudBox(canvas, cx + timerW/2 + gap, 0, cx + timerW/2 + gap + scoreW, boxH, String.valueOf(cage3.getScore()), Color.GREEN);

            // BOTTOM CLUSTER
            float pauseW = 140;
            pauseLeft = cx - pauseW / 2;
            pauseTop = screenHeight - boxH;
            pauseRight = cx + pauseW / 2;
            pauseBottom = screenHeight;

            drawHudBox(canvas, pauseLeft - gap - scoreW, pauseTop, pauseLeft - gap, pauseBottom, String.valueOf(cage1.getScore()), Color.RED);
            drawHudBox(canvas, pauseLeft, pauseTop, pauseRight, pauseBottom, "||", Color.WHITE);
            drawHudBox(canvas, pauseRight + gap, pauseTop, pauseRight + gap + scoreW, pauseBottom, String.valueOf(cage2.getScore()), Color.BLUE);

            // Game Over Overlay with Winner Declaration
            // Game Over Overlay with Winner Declaration and 5-Second Timer
            if (isGameOver) {
                canvas.drawColor(Color.argb(210, 0, 0, 0));

                textPaint.setTextSize(75);
                textPaint.setColor(Color.WHITE);
                canvas.drawText(gameOverTitle, cx, arenaCenterY - 100, textPaint);

                textPaint.setTextSize(55);
                textPaint.setColor(winnerColor);
                canvas.drawText(winnerText, cx, arenaCenterY + 20, textPaint);

                // Calculate how many seconds are left in the 5-second countdown
                long elapsed = System.currentTimeMillis() - gameOverStartTime;
                int returnTimer = 5 - (int) (elapsed / 1000);
                returnTimer = Math.max(0, returnTimer); // Prevent negative numbers

                textPaint.setTextSize(45);
                textPaint.setColor(Color.WHITE);
                canvas.drawText("Returning to menu in " + returnTimer + "...", cx, arenaCenterY + 120, textPaint);

            } else if (isPaused) { // PAUSE OVERLAY BLOCK
                // ... (keep your existing isPaused code here) // PAUSE OVERLAY BLOCK
                canvas.drawColor(Color.argb(210, 0, 0, 0));
                textPaint.setTextSize(80);
                textPaint.setColor(Color.WHITE);
                canvas.drawText("PAUSED", cx, arenaCenterY - 200, textPaint);

                btnResumeTop = arenaCenterY - 100;
                btnRestartTop = arenaCenterY + 20;
                btnMenuTop = arenaCenterY + 140;

                drawMenuBtn(canvas, "RESUME", cx, btnResumeTop, Color.GREEN);
                drawMenuBtn(canvas, "RESTART", cx, btnRestartTop, Color.YELLOW);
                drawMenuBtn(canvas, "MENU", cx, btnMenuTop, Color.RED);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawHudBox(Canvas canvas, float left, float top, float right, float bottom, String text, int textColor) {
        canvas.drawRect(left, top, right, bottom, uiBgPaint);
        textPaint.setColor(textColor);
        float textY = top + ((bottom - top) / 2) - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(text, left + ((right - left) / 2), textY, textPaint);
        textPaint.setColor(Color.WHITE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);

        float x = event.getX(actionIndex);
        float y = event.getY(actionIndex);

        switch (action) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Ignore all touches if the game is in the 5-second Game Over countdown
                if (isGameOver) return true;

                // Handle Pause Screen Clicks
                if (isPaused) {
                    float cx = screenWidth / 2f;
                    // ... (keep the rest of your pause menu touch logic)
                    if (x > cx - btnWidth / 2 && x < cx + btnWidth / 2) {
                        if (y > btnResumeTop && y < btnResumeTop + btnHeight) {
                            isPaused = false;
                        } else if (y > btnRestartTop && y < btnRestartTop + btnHeight) {
                            resetGame();
                        } else if (y > btnMenuTop && y < btnMenuTop + btnHeight) {
                            Intent intent = new Intent(getContext(), MainActivity.class); // 👈 Replace MainActivity with your select player screen Activity name
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            getContext().startActivity(intent);
                            ((Activity) getContext()).finish();
                        }
                    }
                    return true;
                }

                if (x >= pauseLeft && x <= pauseRight && y >= pauseTop && y <= pauseBottom) {
                    isPaused = true;
                    return true;
                }

                if (joystick1 != null && joystick1.isPressed(x, y) && !joystick1.getIsPressed()) { joystick1.setPointerId(pointerId); }
                else if (joystick2 != null && joystick2.isPressed(x, y) && !joystick2.getIsPressed()) { joystick2.setPointerId(pointerId); }
                else if (joystick3 != null && joystick3.isPressed(x, y) && !joystick3.getIsPressed()) { joystick3.setPointerId(pointerId); }
                else if (joystick4 != null && joystick4.isPressed(x, y) && !joystick4.getIsPressed()) { joystick4.setPointerId(pointerId); }
                return true;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    float touchX = event.getX(i);
                    float touchY = event.getY(i);

                    if (joystick1 != null && joystick1.getIsPressed() && joystick1.getPointerId() == id) { joystick1.update(touchX, touchY); }
                    if (joystick2 != null && joystick2.getIsPressed() && joystick2.getPointerId() == id) { joystick2.update(touchX, touchY); }
                    if (joystick3 != null && joystick3.getIsPressed() && joystick3.getPointerId() == id) { joystick3.update(touchX, touchY); }
                    if (joystick4 != null && joystick4.getIsPressed() && joystick4.getPointerId() == id) { joystick4.update(touchX, touchY); }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (joystick1 != null && joystick1.getIsPressed() && joystick1.getPointerId() == pointerId) { joystick1.reset(); }
                if (joystick2 != null && joystick2.getIsPressed() && joystick2.getPointerId() == pointerId) { joystick2.reset(); }
                if (joystick3 != null && joystick3.getIsPressed() && joystick3.getPointerId() == pointerId) { joystick3.reset(); }
                if (joystick4 != null && joystick4.getIsPressed() && joystick4.getPointerId() == pointerId) { joystick4.reset(); }
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    private void drawMenuBtn(Canvas canvas, String text, float cx, float top, int color) {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(cx - btnWidth / 2, top, cx + btnWidth / 2, top + btnHeight, paint);

        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawRect(cx - btnWidth / 2, top, cx + btnWidth / 2, top + btnHeight, paint);

        textPaint.setTextSize(45);
        textPaint.setColor(color);
        canvas.drawText(text, cx, top + (btnHeight / 2) - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);
        paint.setStyle(Paint.Style.FILL);
    }
}