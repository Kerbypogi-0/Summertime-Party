package com.kerbcorp.cessbomb;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * GameView draws the current summer-party scene, hosts every tappable
 * object, and handles hit-testing plus the bomb-explosion / correct-find
 * feedback animations. It owns the stage/level data directly so it can be
 * dropped into the Cess Bomb module as a single file if you don't want
 * separate model classes.
 *
 * Wire it up from CessBombActivity via setListener(...) and call
 * loadStage(int) to show a given stage (0-indexed).
 */
public class GameView extends View {

    // ---------- Public callback contract ----------

    public interface Listener {
        /** Called immediately after a tap is resolved. */
        void onAnswer(boolean correct, int livesRemaining);

        /** Called once lives hit zero or every stage has been cleared. */
        void onGameOver(boolean cleared, int stageReached);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    // ---------- Scene object model ----------

    private enum Shape { CIRCLE, RECT, TRIANGLE }

    private static class Obj {
        final String id;
        final Shape shape;
        final float cx, cy, size; // fractions of view size
        final int color;

        Obj(String id, Shape shape, float cx, float cy, float size, int color) {
            this.id = id;
            this.shape = shape;
            this.cx = cx;
            this.cy = cy;
            this.size = size;
            this.color = color;
        }

        boolean contains(float px, float py, float w, float h) {
            float dx = (px - cx) * w;
            float dy = (py - cy) * h;
            float radiusPx = size * Math.min(w, h);
            float tolerance = radiusPx * 1.35f; // easier to tap on phones
            return (dx * dx + dy * dy) <= (tolerance * tolerance);
        }
    }

    private static class Stage {
        final String question;
        final List<Obj> objects;
        final String correctId;

        Stage(String question, List<Obj> objects, String correctId) {
            this.question = question;
            this.objects = objects;
            this.correctId = correctId;
        }
    }

    private final List<Stage> stages = buildStages();

    private static List<Stage> buildStages() {
        List<Stage> list = new ArrayList<>();

        List<Obj> s1 = new ArrayList<>();
        s1.add(new Obj("ball", Shape.CIRCLE, 0.30f, 0.55f, 0.10f, Color.parseColor("#FF6B6B")));
        s1.add(new Obj("sun", Shape.CIRCLE, 0.80f, 0.18f, 0.09f, Color.parseColor("#FFD93D")));
        s1.add(new Obj("towel", Shape.RECT, 0.62f, 0.72f, 0.09f, Color.parseColor("#00A8CC")));
        s1.add(new Obj("shell", Shape.TRIANGLE, 0.18f, 0.80f, 0.06f, Color.parseColor("#FFFFFF")));
        list.add(new Stage("Tap the BEACH BALL hiding in the scene!", s1, "ball"));

        List<Obj> s2 = new ArrayList<>();
        s2.add(new Obj("shades", Shape.RECT, 0.50f, 0.40f, 0.08f, Color.parseColor("#26333F")));
        s2.add(new Obj("iceCream", Shape.TRIANGLE, 0.22f, 0.60f, 0.07f, Color.parseColor("#FFC93C")));
        s2.add(new Obj("flipflop", Shape.RECT, 0.78f, 0.75f, 0.07f, Color.parseColor("#FF6B6B")));
        s2.add(new Obj("starfish", Shape.TRIANGLE, 0.65f, 0.25f, 0.06f, Color.parseColor("#FFD93D")));
        list.add(new Stage("Which object protects your eyes from the sun?", s2, "shades"));

        List<Obj> s3 = new ArrayList<>();
        s3.add(new Obj("umbrella", Shape.TRIANGLE, 0.50f, 0.30f, 0.12f, Color.parseColor("#FF6B6B")));
        s3.add(new Obj("crab", Shape.CIRCLE, 0.20f, 0.70f, 0.06f, Color.parseColor("#E63946")));
        s3.add(new Obj("cooler", Shape.RECT, 0.75f, 0.65f, 0.08f, Color.parseColor("#00A8CC")));
        list.add(new Stage("Tap the object that gives you shade on a hot day.", s3, "umbrella"));

        List<Obj> s4 = new ArrayList<>();
        s4.add(new Obj("surfboard", Shape.RECT, 0.45f, 0.50f, 0.07f, Color.parseColor("#FFD93D")));
        s4.add(new Obj("fish", Shape.TRIANGLE, 0.75f, 0.30f, 0.06f, Color.parseColor("#00A8CC")));
        s4.add(new Obj("hat", Shape.CIRCLE, 0.25f, 0.25f, 0.08f, Color.parseColor("#F4D19B")));
        list.add(new Stage("Tap the board you'd ride a wave on!", s4, "surfboard"));

        List<Obj> s5 = new ArrayList<>();
        s5.add(new Obj("castle", Shape.TRIANGLE, 0.55f, 0.65f, 0.10f, Color.parseColor("#F4D19B")));
        s5.add(new Obj("kite", Shape.RECT, 0.25f, 0.20f, 0.07f, Color.parseColor("#FF6B6B")));
        s5.add(new Obj("boat", Shape.TRIANGLE, 0.75f, 0.55f, 0.07f, Color.parseColor("#00A8CC")));
        list.add(new Stage("Tap what you'd build out of sand at the shore.", s5, "castle"));

        return list;
    }

    // ---------- Runtime state ----------

    public static final int STARTING_LIVES = 3;

    private int stageIndex = 0;
    private int lives = STARTING_LIVES;
    private boolean inputLocked = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean exploding = false;
    private float explosionCx, explosionCy, explosionRadius;

    private boolean celebrating = false;
    private float celebrateCx, celebrateCy, celebrateRadius;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Resets progress and shows stage 0. Call this once the view has a size. */
    public void startNewGame() {
        stageIndex = 0;
        lives = STARTING_LIVES;
        inputLocked = false;
        exploding = false;
        celebrating = false;
        invalidate();
    }

    public String currentQuestion() {
        return stages.get(stageIndex).question;
    }

    public int currentStageNumber() {
        return stageIndex + 1;
    }

    public int livesRemaining() {
        return lives;
    }

    // ---------- Drawing ----------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        drawBackground(canvas, w, h);

        Stage stage = stages.get(stageIndex);
        for (Obj obj : stage.objects) {
            drawObject(canvas, obj, w, h);
        }

        if (exploding) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#CCFF4500"));
            canvas.drawCircle(explosionCx, explosionCy, explosionRadius, paint);
            paint.setColor(Color.parseColor("#FFFF8C00"));
            canvas.drawCircle(explosionCx, explosionCy, explosionRadius * 0.6f, paint);
        }

        if (celebrating) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10f);
            paint.setColor(Color.parseColor("#2EC4B6"));
            canvas.drawCircle(celebrateCx, celebrateCy, celebrateRadius, paint);
        }
    }

    private void drawBackground(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FFC93C"));
        canvas.drawRect(0, 0, w, h * 0.60f, paint);

        paint.setColor(Color.parseColor("#00A8CC"));
        canvas.drawRect(0, h * 0.60f, w, h * 0.78f, paint);

        paint.setColor(Color.parseColor("#F4D19B"));
        canvas.drawRect(0, h * 0.78f, w, h, paint);
    }

    private void drawObject(Canvas canvas, Obj obj, int w, int h) {
        float cx = obj.cx * w;
        float cy = obj.cy * h;
        float size = obj.size * Math.min(w, h);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(obj.color);

        switch (obj.shape) {
            case CIRCLE:
                canvas.drawCircle(cx, cy, size, paint);
                break;
            case RECT:
                RectF rect = new RectF(cx - size, cy - size * 0.6f, cx + size, cy + size * 0.6f);
                canvas.drawRoundRect(rect, 12f, 12f, paint);
                break;
            case TRIANGLE:
                Path path = new Path();
                path.moveTo(cx, cy - size);
                path.lineTo(cx - size, cy + size);
                path.lineTo(cx + size, cy + size);
                path.close();
                canvas.drawPath(path, paint);
                break;
        }
    }

    // ---------- Input ----------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (inputLocked) return true;
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        int w = getWidth();
        int h = getHeight();
        float px = event.getX();
        float py = event.getY();

        Stage stage = stages.get(stageIndex);
        for (Obj obj : stage.objects) {
            if (obj.contains(px / w, py / h, w, h)) {
                boolean correct = obj.id.equals(stage.correctId);
                inputLocked = true;
                if (correct) {
                    playCelebration(obj.cx * w, obj.cy * h);
                } else {
                    lives--;
                    playExplosion(px, py);
                }
                if (listener != null) {
                    listener.onAnswer(correct, lives);
                }
                postDelayed(() -> resolveAfterAnswer(correct), correct ? 650 : 550);
                return true;
            }
        }
        return true;
    }

    private void resolveAfterAnswer(boolean lastWasCorrect) {
        exploding = false;
        celebrating = false;

        if (lastWasCorrect) {
            stageIndex++;
            if (stageIndex >= stages.size()) {
                if (listener != null) listener.onGameOver(true, stages.size());
                return;
            }
        } else if (lives <= 0) {
            if (listener != null) listener.onGameOver(false, stageIndex + 1);
            return;
        }

        inputLocked = false;
        invalidate();
    }

    // ---------- Feedback animations ----------

    private void playExplosion(float x, float y) {
        exploding = true;
        explosionCx = x;
        explosionCy = y;
        ValueAnimator anim = ValueAnimator.ofFloat(0f, Math.max(getWidth(), getHeight()) * 0.25f);
        anim.setDuration(350);
        anim.addUpdateListener(a -> {
            explosionRadius = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.start();

        ValueAnimator shake = ValueAnimator.ofFloat(-18f, 18f);
        shake.setDuration(60);
        shake.setRepeatCount(5);
        shake.setRepeatMode(ValueAnimator.REVERSE);
        shake.addUpdateListener(a -> setTranslationX((float) a.getAnimatedValue()));
        shake.start();
    }

    private void playCelebration(float x, float y) {
        celebrating = true;
        celebrateCx = x;
        celebrateCy = y;
        ValueAnimator anim = ValueAnimator.ofFloat(20f, 90f);
        anim.setDuration(300);
        anim.addUpdateListener(a -> {
            celebrateRadius = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.start();
    }
}