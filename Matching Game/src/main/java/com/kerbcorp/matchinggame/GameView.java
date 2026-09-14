package com.kerbcorp.matchinggame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the card grid on a Canvas and turns taps into game moves.
 * Change COLUMNS / ROWS below to resize the grid (must multiply to an even number).
 */
public class GameView extends View {

    public interface GameListener {
        void onMove(int moveCount);
        void onMatchFound(int matchedPairs, int totalPairs);
        void onGameComplete(int moveCount);
    }

    private static final int COLUMNS = 4;
    private static final int ROWS = 4;
    private static final long FLIP_BACK_DELAY_MS = 700;

    private static final float PADDING = 24f;
    private static final float GAP = 14f;
    private static final float CORNER_RADIUS = 20f;

    private final MatchingGame game;
    private GameListener listener;

    private final Paint cardBackPaint = new Paint();
    private final Paint cardFrontPaint = new Paint();
    private final Paint matchedPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint symbolPaint = new Paint();

    private final List<RectF> cardBounds = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean inputLocked = false;

    public GameView(Context context) {
        super(context);
        game = new MatchingGame((COLUMNS * ROWS) / 2);
        init();
    }

    private void init() {
        cardBackPaint.setColor(Color.parseColor("#14B8A6"));
        cardBackPaint.setStyle(Paint.Style.FILL);
        cardBackPaint.setAntiAlias(true);

        cardFrontPaint.setColor(Color.parseColor("#FFFDF8"));
        cardFrontPaint.setStyle(Paint.Style.FILL);
        cardFrontPaint.setAntiAlias(true);

        matchedPaint.setColor(Color.parseColor("#EEF7F0"));
        matchedPaint.setStyle(Paint.Style.FILL);
        matchedPaint.setAntiAlias(true);

        borderPaint.setColor(Color.parseColor("#0F766E"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setAntiAlias(true);

        symbolPaint.setColor(Color.parseColor("#1C1A2B"));
        symbolPaint.setTextAlign(Paint.Align.CENTER);
        symbolPaint.setAntiAlias(true);
        symbolPaint.setTextSize(64f);

        setClickable(true);
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    public int getTotalPairs() {
        return game.getTotalPairs();
    }

    public void resetGame() {
        game.reset();
        inputLocked = false;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeCardBounds(w, h);
    }

    private void computeCardBounds(int width, int height) {
        cardBounds.clear();

        float usableWidth = width - PADDING * 2 - GAP * (COLUMNS - 1);
        float usableHeight = height - PADDING * 2 - GAP * (ROWS - 1);
        float cardSize = Math.min(usableWidth / COLUMNS, usableHeight / ROWS);

        float gridWidth = cardSize * COLUMNS + GAP * (COLUMNS - 1);
        float gridHeight = cardSize * ROWS + GAP * (ROWS - 1);
        float startX = (width - gridWidth) / 2f;
        float startY = (height - gridHeight) / 2f;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                float left = startX + col * (cardSize + GAP);
                float top = startY + row * (cardSize + GAP);
                cardBounds.add(new RectF(left, top, left + cardSize, top + cardSize));
            }
        }
        symbolPaint.setTextSize(cardSize * 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<MatchingGame.Card> cards = game.getCards();

        for (int i = 0; i < cardBounds.size() && i < cards.size(); i++) {
            RectF bounds = cardBounds.get(i);
            MatchingGame.Card card = cards.get(i);

            if (card.matched) {
                canvas.drawRoundRect(bounds, CORNER_RADIUS, CORNER_RADIUS, matchedPaint);
                canvas.drawRoundRect(bounds, CORNER_RADIUS, CORNER_RADIUS, borderPaint);
                drawSymbol(canvas, bounds, card.symbol);
            } else if (card.faceUp) {
                canvas.drawRoundRect(bounds, CORNER_RADIUS, CORNER_RADIUS, cardFrontPaint);
                drawSymbol(canvas, bounds, card.symbol);
            } else {
                canvas.drawRoundRect(bounds, CORNER_RADIUS, CORNER_RADIUS, cardBackPaint);
            }
        }
    }

    private void drawSymbol(Canvas canvas, RectF bounds, String symbol) {
        Paint.FontMetrics fm = symbolPaint.getFontMetrics();
        float centerX = bounds.centerX();
        float centerY = bounds.centerY() - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(symbol, centerX, centerY, symbolPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !inputLocked) {
            int index = findCardIndexAt(event.getX(), event.getY());
            if (index >= 0) {
                handleCardTap(index);
            }
        }
        return true;
    }

    private int findCardIndexAt(float x, float y) {
        for (int i = 0; i < cardBounds.size(); i++) {
            if (cardBounds.get(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void handleCardTap(int index) {
        boolean flipped = game.flipCard(index);
        if (!flipped) return;
        invalidate();

        if (game.isPairPending()) {
            inputLocked = true;
            handler.postDelayed(() -> {
                boolean isMatch = game.resolvePendingPair();
                if (listener != null) {
                    listener.onMove(game.getMoveCount());
                    if (isMatch) {
                        listener.onMatchFound(game.getMatchedPairCount(), game.getTotalPairs());
                    }
                }
                inputLocked = false;
                invalidate();

                if (game.isComplete() && listener != null) {
                    listener.onGameComplete(game.getMoveCount());
                }
            }, FLIP_BACK_DELAY_MS);
        }
    }
}