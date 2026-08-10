package com.kerbcorp.goski;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class GameView extends View {

    private Paint paint;

    public GameView(Context context) {
        super(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Water background
        canvas.drawColor(Color.rgb(60, 180, 220));

        // Draw the race track
        drawTrack(canvas);

        // Draw the four players
        drawPlayers(canvas);
    }

    private void drawTrack(Canvas canvas) {

        float laneHeight = getHeight() / 5f;

        // White race lanes
        paint.setColor(Color.rgb(225, 245, 245));
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 4; i++) {

            float top = i * laneHeight;

            canvas.drawRect(
                    0,
                    top,
                    getWidth(),
                    top + laneHeight,
                    paint
            );
        }

        // Lane separators
        paint.setColor(Color.rgb(150, 200, 205));
        paint.setStrokeWidth(4);

        for (int i = 1; i < 4; i++) {

            float y = i * laneHeight;

            canvas.drawLine(
                    0,
                    y,
                    getWidth(),
                    y,
                    paint
            );
        }

        // Finish line
        float finishX = getWidth() - 100;

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8);

        canvas.drawLine(
                finishX,
                0,
                finishX,
                getHeight()
        );

        // Finish text
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(
                "FINISH",
                finishX + 35,
                getHeight() / 2f,
                paint
        );
    }

    private void drawPlayers(Canvas canvas) {

        float laneHeight = getHeight() / 5f;

        int[] colors = {
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.YELLOW
        };

        for (int i = 0; i < 4; i++) {

            float x = 100;

            float y =
                    i * laneHeight
                            + laneHeight / 2f;

            drawJetSki(
                    canvas,
                    x,
                    y,
                    colors[i],
                    i + 1
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

        // Jet ski body
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        RectF body = new RectF(
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
        paint.setColor(Color.DKGRAY);

        canvas.drawCircle(
                x,
                y - 22,
                9,
                paint
        );

        // Player number
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);

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
}