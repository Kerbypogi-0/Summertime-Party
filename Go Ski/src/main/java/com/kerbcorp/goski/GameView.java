package com.kerbcorp.goski;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    private Paint paint;

    private GoSkiPlayer[] players;

    private int numPlayers;

    private boolean raceStarted = false;
    private boolean raceFinished = false;

    private int winner = 0;

    // Bitmaps
    private Bitmap trackBitmap;
    private Bitmap logoBitmap;
    private Bitmap[] playerBitmaps;

    // Confetti
    private List<Confetti> confettiList = new ArrayList<>();
    private Random random = new Random();
    private long lastFrameTime = 0;

    private int[] confettiColors = {
            Color.rgb(255, 87, 87),
            Color.rgb(255, 205, 60),
            Color.rgb(90, 200, 130),
            Color.rgb(90, 160, 255),
            Color.rgb(200, 120, 255)
    };

    public interface OnRaceFinishedListener {
        void onRaceFinished(int winner);
    }

    private OnRaceFinishedListener finishListener;

    public void setOnRaceFinishedListener(OnRaceFinishedListener listener) {
        this.finishListener = listener;
    }

    public GameView(Context context, int numPlayers) {

        super(context);

        this.numPlayers = numPlayers;

        paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
        );

        players = new GoSkiPlayer[numPlayers];

        for (int i = 0; i < numPlayers; i++) {
            players[i] = new GoSkiPlayer(i + 1);
        }

        trackBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.go_ski_track
        );

        logoBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.go_ski_logo
        );

        playerBitmaps = new Bitmap[] {
                BitmapFactory.decodeResource(getResources(), R.drawable.player1_red),
                BitmapFactory.decodeResource(getResources(), R.drawable.player2_blue),
                BitmapFactory.decodeResource(getResources(), R.drawable.player3_green),
                BitmapFactory.decodeResource(getResources(), R.drawable.player4_yellow)
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (trackBitmap != null) {
            Rect destRect = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(trackBitmap, null, destRect, paint);
        } else {
            canvas.drawColor(Color.rgb(50, 180, 220));
        }

        drawTrackOverlay(canvas);
        drawPlayers(canvas);

        if (!raceStarted && logoBitmap != null) {
            drawLogo(canvas);
        }

        if (raceFinished) {
            updateConfetti();
            drawWinner(canvas, winner);
            invalidate();
        }
    }

    private void drawTrackOverlay(Canvas canvas) {

        float laneHeight = getHeight() / (numPlayers + 1f);

        paint.setColor(Color.argb(120, 255, 255, 255));
        paint.setStrokeWidth(4);

        for (int i = 1; i < numPlayers; i++) {
            float y = i * laneHeight;
            canvas.drawLine(0, y, getWidth(), y, paint);
        }

        float finishX = getWidth() - 100;

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8);

        canvas.drawLine(finishX, 0, finishX, getHeight(), paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(25);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("FINISH", finishX, 35, paint);
    }

    private void drawPlayers(Canvas canvas) {

        float laneHeight = getHeight() / (numPlayers + 1f);

        int spriteHeight = (int) (laneHeight * 0.7f);

        for (int i = 0; i < numPlayers; i++) {

            GoSkiPlayer player = players[i];

            float x = 70 + player.getProgress() * (getWidth() - 190);
            float y = i * laneHeight + laneHeight / 2f;

            drawJetSki(canvas, x, y, playerBitmaps[i], spriteHeight);

            paint.setColor(Color.BLACK);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.LEFT);

            canvas.drawText(
                    "P" + player.getPlayerNumber() + ": " + player.getTapCount() + " taps",
                    10,
                    i * laneHeight + 25,
                    paint
            );
        }
    }

    private void drawJetSki(
            Canvas canvas,
            float x,
            float y,
            Bitmap bitmap,
            int spriteHeight
    ) {

        if (bitmap == null) {
            return;
        }

        float aspect = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        int spriteWidth = (int) (spriteHeight * aspect);

        Rect destRect = new Rect(
                (int) (x - spriteWidth / 2f),
                (int) (y - spriteHeight / 2f),
                (int) (x + spriteWidth / 2f),
                (int) (y + spriteHeight / 2f)
        );

        canvas.drawBitmap(bitmap, null, destRect, paint);
    }

    private void drawLogo(Canvas canvas) {

        int logoWidth = getWidth() / 2;
        float aspect = (float) logoBitmap.getWidth() / (float) logoBitmap.getHeight();
        int logoHeight = (int) (logoWidth / aspect);

        int left = (getWidth() - logoWidth) / 2;
        int top = (getHeight() - logoHeight) / 2;

        Rect destRect = new Rect(left, top, left + logoWidth, top + logoHeight);

        canvas.drawBitmap(logoBitmap, null, destRect, paint);
    }

    public void startRace() {

        raceStarted = true;
        raceFinished = false;
        winner = 0;
        confettiList.clear();

        invalidate();
    }

    public void playerTap(int playerNumber) {

        if (!raceStarted) {
            return;
        }

        if (raceFinished) {
            return;
        }

        if (playerNumber < 1 || playerNumber > numPlayers) {
            return;
        }

        GoSkiPlayer player = players[playerNumber - 1];

        player.tap();

        if (player.isFinished()) {

            winner = player.getPlayerNumber();
            raceFinished = true;

            spawnConfetti();

            if (finishListener != null) {
                finishListener.onRaceFinished(winner);
            }
        }

        invalidate();
    }

    private void spawnConfetti() {

        confettiList.clear();

        lastFrameTime = System.currentTimeMillis();

        for (int i = 0; i < 60; i++) {

            Confetti c = new Confetti();

            c.x = random.nextFloat() * getWidth();
            c.y = -random.nextFloat() * getHeight();
            c.speed = 150 + random.nextFloat() * 250;
            c.size = 8 + random.nextFloat() * 10;
            c.color = confettiColors[random.nextInt(confettiColors.length)];
            c.drift = (random.nextFloat() - 0.5f) * 80;

            confettiList.add(c);
        }
    }

    private void updateConfetti() {

        long now = System.currentTimeMillis();

        float dt = (now - lastFrameTime) / 1000f;

        lastFrameTime = now;

        for (Confetti c : confettiList) {

            c.y += c.speed * dt;
            c.x += c.drift * dt;

            if (c.y > getHeight()) {
                c.y = -20;
                c.x = random.nextFloat() * getWidth();
            }
        }
    }

    private void drawWinner(Canvas canvas, int winner) {

        paint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        for (Confetti c : confettiList) {
            paint.setColor(c.color);
            canvas.drawRect(c.x, c.y, c.x + c.size, c.y + c.size, paint);
        }

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        paint.setColor(playerBadgeColor(winner));
        canvas.drawCircle(centerX, centerY - 80, 60, paint);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50);

        canvas.drawText("P" + winner, centerX, centerY - 65, paint);

        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(45);

        canvas.drawText("\u2605", centerX, centerY - 150, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(48);

        canvas.drawText("PLAYER " + winner + " WINS!", centerX, centerY + 30, paint);

        paint.setColor(Color.rgb(230, 230, 230));
        paint.setTextSize(22);

        canvas.drawText("Great race!", centerX, centerY + 70, paint);
    }

    private int playerBadgeColor(int playerNumber) {

        int[] colors = {
                Color.rgb(230, 60, 60),
                Color.rgb(60, 120, 230),
                Color.rgb(70, 180, 90),
                Color.rgb(230, 190, 40)
        };

        return colors[(playerNumber - 1) % colors.length];
    }

    public void resetRace() {

        for (GoSkiPlayer player : players) {
            player.reset();
        }

        raceStarted = false;
        raceFinished = false;
        winner = 0;

        confettiList.clear();

        invalidate();
    }

    private static class Confetti {
        float x, y, speed, size, drift;
        int color;
    }
}