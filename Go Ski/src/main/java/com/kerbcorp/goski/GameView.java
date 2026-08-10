package com.kerbcorp.goski;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class GameView extends View {

    private final Paint paint;

    private final GoSkiPlayer[] players;

    private boolean raceStarted = false;
    private boolean raceFinished = false;

    private int winner = 0;

    private final int[] playerColors = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW
    };

    public GameView(Context context) {

        super(context);

        paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
        );

        players = new GoSkiPlayer[] {

                new GoSkiPlayer(1),

                new GoSkiPlayer(2),

                new GoSkiPlayer(3),

                new GoSkiPlayer(4)
        };
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {

        super.onDraw(canvas);

        // Draw water
        canvas.drawColor(
                Color.rgb(50, 180, 220)
        );

        // Draw track
        drawTrack(canvas);

        // Draw players
        drawPlayers(canvas);

        // Draw winner
        if (raceFinished) {

            drawWinner(
                    canvas,
                    winner
            );
        }
    }

    private void drawTrack(Canvas canvas) {

        float laneHeight =
                getHeight() / 5f;

        // Race lanes
        paint.setColor(
                Color.rgb(225, 245, 245)
        );

        paint.setStyle(
                Paint.Style.FILL
        );

        for (int i = 0; i < 4; i++) {

            float top =
                    i * laneHeight;

            canvas.drawRect(
                    0,
                    top,
                    getWidth(),
                    top + laneHeight,
                    paint
            );
        }

        // Lane lines
        paint.setColor(
                Color.rgb(150, 200, 205)
        );

        paint.setStrokeWidth(4);

        for (int i = 1; i < 4; i++) {

            float y =
                    i * laneHeight;

            canvas.drawLine(
                    0,
                    y,
                    getWidth(),
                    y,
                    paint
            );
        }

        // Finish line
        float finishX =
                getWidth() - 100;

        paint.setColor(Color.BLACK);

        paint.setStrokeWidth(8);

        canvas.drawLine(
                finishX,
                0,
                finishX,
                getHeight(),
                paint
        );
        // Finish text
        paint.setColor(Color.BLACK);

        paint.setTextSize(25);

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                "FINISH",
                finishX,
                35,
                paint
        );
    }

    private void drawPlayers(Canvas canvas) {

        float laneHeight =
                getHeight() / 5f;

        for (int i = 0; i < 4; i++) {

            GoSkiPlayer player =
                    players[i];

            float x =
                    70 +
                            player.getProgress()
                                    * (getWidth() - 190);

            float y =
                    i * laneHeight
                            + laneHeight / 2f;

            drawJetSki(
                    canvas,
                    x,
                    y,
                    playerColors[i],
                    player.getPlayerNumber()
            );

            // Tap count
            paint.setColor(Color.BLACK);

            paint.setTextSize(18);

            paint.setTextAlign(
                    Paint.Align.LEFT
            );

            canvas.drawText(
                    "P" +
                            player.getPlayerNumber() +
                            ": " +
                            player.getTapCount() +
                            " taps",
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
            int color,
            int playerNumber
    ) {

        // Jet ski
        paint.setColor(color);

        paint.setStyle(
                Paint.Style.FILL
        );

        RectF body =
                new RectF(
                        x - 30,
                        y - 15,
                        x + 35,
                        y + 15
                );

        canvas.drawRoundRect(
                body,
                15,
                15,
                paint
        );

        // Rider
        paint.setColor(
                Color.DKGRAY
        );

        canvas.drawCircle(
                x,
                y - 22,
                9,
                paint
        );

        // Player number
        paint.setColor(Color.BLACK);

        paint.setTextSize(18);

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                String.valueOf(playerNumber),
                x,
                y + 7,
                paint
        );

        // Water splash
        paint.setColor(Color.WHITE);

        canvas.drawCircle(
                x - 40,
                y,
                5,
                paint
        );

        canvas.drawCircle(
                x - 50,
                y - 8,
                3,
                paint
        );

        canvas.drawCircle(
                x - 50,
                y + 8,
                3,
                paint
        );
    }

    public void startRace() {

        raceStarted = true;

        raceFinished = false;

        winner = 0;

        invalidate();
    }

    public void playerTap(
            int playerNumber
    ) {

        // Don't allow tapping before GO
        if (!raceStarted) {
            return;
        }

        // Don't allow tapping after race
        if (raceFinished) {
            return;
        }

        GoSkiPlayer player =
                players[playerNumber - 1];

        player.tap();

        // Check if player reached finish
        if (player.isFinished()) {

            winner =
                    player.getPlayerNumber();

            raceFinished = true;
        }

        invalidate();
    }

    private void drawWinner(
            Canvas canvas,
            int winner
    ) {

        // Dark transparent overlay
        paint.setColor(
                Color.argb(
                        180,
                        0,
                        0,
                        0
                )
        );

        canvas.drawRect(
                0,
                0,
                getWidth(),
                getHeight(),
                paint
        );

        paint.setColor(Color.WHITE);

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setTextSize(50);

        canvas.drawText(
                "PLAYER " +
                        winner +
                        " WINS!",
                getWidth() / 2f,
                getHeight() / 2f,
                paint
        );
    }

    public void resetRace() {

        for (
                GoSkiPlayer player :
                players
        ) {

            player.reset();
        }

        raceStarted = false;

        raceFinished = false;

        winner = 0;

        invalidate();
    }
}