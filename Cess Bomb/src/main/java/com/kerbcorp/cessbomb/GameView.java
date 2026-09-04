package com.kerbcorp.cessbomb;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * GameView draws the current trivia question as a row of colored
 * "answer balls" with the option text on each one, over a drawn beach
 * scene (sky, sun, clouds, sea with waves, sand, palm tree). Hosts
 * hit-testing and plays a celebration ring on a correct tap or a
 * burst/pop animation on a wrong one. Owns all difficulty/question
 * data and score tracking directly.
 */
public class GameView extends View {

    public enum Difficulty { EASY, MEDIUM, HARD }

    public interface Listener {
        void onAnswer(boolean correct, int livesRemaining, int score);
        void onGameOver(boolean cleared, int stageReached, int score, int totalStages);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    // ---------- Question / option model ----------

    private static class Option {
        final String id;
        final String label;
        final float cx, cy, radius; // fractions of view size
        final int color;

        Option(String id, String label, float cx, float cy, float radius, int color) {
            this.id = id;
            this.label = label;
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.color = color;
        }

        boolean contains(float px, float py, float w, float h) {
            float dx = (px - cx) * w;
            float dy = (py - cy) * h;
            float r = radius * Math.min(w, h);
            float tolerance = r * 1.15f;
            return (dx * dx + dy * dy) <= (tolerance * tolerance);
        }
    }

    private static class Question {
        final String prompt;
        final List<Option> options;
        final String correctId;

        Question(String prompt, List<Option> options, String correctId) {
            this.prompt = prompt;
            this.options = options;
            this.correctId = correctId;
        }
    }

    private static final int[] BALL_COLORS = {
            Color.parseColor("#FF6B6B"), Color.parseColor("#00A8CC"), Color.parseColor("#FFD93D")
    };

    private static List<Option> threeOptions(String a, String b, String c) {
        List<Option> opts = new ArrayList<>();
        opts.add(new Option("a", a, 0.22f, 0.50f, 0.15f, BALL_COLORS[0]));
        opts.add(new Option("b", b, 0.50f, 0.50f, 0.15f, BALL_COLORS[1]));
        opts.add(new Option("c", c, 0.78f, 0.50f, 0.15f, BALL_COLORS[2]));
        return opts;
    }

    private static Map<Difficulty, List<Question>> buildAllQuestions() {
        Map<Difficulty, List<Question>> map = new EnumMap<>(Difficulty.class);

        List<Question> easy = new ArrayList<>();
        easy.add(new Question("What frozen treat cools you down at the beach?",
                threeOptions("Ice Cream", "Soup", "Toast"), "a"));
        easy.add(new Question("What do you wear to protect your eyes from the sun?",
                threeOptions("Mittens", "Sunglasses", "Scarf"), "b"));
        easy.add(new Question("What do you build out of wet sand at the shore?",
                threeOptions("Igloo", "Treehouse", "Sandcastle"), "c"));
        map.put(Difficulty.EASY, easy);

        List<Question> medium = new ArrayList<>();
        medium.add(new Question("Which board do you ride on ocean waves?",
                threeOptions("Skateboard", "Surfboard", "Snowboard"), "b"));
        medium.add(new Question("What gives you shade on a hot beach day?",
                threeOptions("Umbrella", "Raincoat", "Blanket"), "a"));
        medium.add(new Question("What fruit is famously sliced for summer picnics?",
                threeOptions("Pumpkin", "Cranberry", "Watermelon"), "c"));
        map.put(Difficulty.MEDIUM, medium);

        List<Question> hard = new ArrayList<>();
        hard.add(new Question("Which sea creature famously walks sideways on the shore?",
                threeOptions("Crab", "Dolphin", "Seagull"), "a"));
        hard.add(new Question("What instrument is classically played at a luau?",
                threeOptions("Bagpipes", "Ukulele", "Trombone"), "b"));
        hard.add(new Question("What ocean phenomenon pulls swimmers away from shore?",
                threeOptions("Tide pool", "Rip current", "Low tide"), "b"));
        map.put(Difficulty.HARD, hard);

        return map;
    }

    private final Map<Difficulty, List<Question>> allQuestions = buildAllQuestions();

    // ---------- Runtime state ----------

    public static final int STARTING_LIVES = 3;

    private Difficulty difficulty = Difficulty.EASY;
    private List<Question> activeQuestions;
    private int stageIndex = 0;
    private int lives = STARTING_LIVES;
    private int score = 0;
    private boolean inputLocked = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String poppingOptionId = null;
    private float popProgress = 0f;

    private boolean celebrating = false;
    private float celebrateCx, celebrateCy, celebrateRadius;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startNewGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.activeQuestions = allQuestions.get(difficulty);
        stageIndex = 0;
        lives = STARTING_LIVES;
        score = 0;
        inputLocked = false;
        poppingOptionId = null;
        celebrating = false;
        invalidate();
    }

    public Difficulty currentDifficulty() { return difficulty; }
    public String currentQuestion() { return activeQuestions.get(stageIndex).prompt; }
    public int currentStageNumber() { return stageIndex + 1; }
    public int totalStages() { return activeQuestions.size(); }
    public int livesRemaining() { return lives; }
    public int currentScore() { return score; }

    // ---------- Drawing ----------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0 || activeQuestions == null) return;

        drawBackground(canvas, w, h);

        Question q = activeQuestions.get(stageIndex);
        for (Option opt : q.options) {
            if (opt.id.equals(poppingOptionId)) {
                drawPop(canvas, opt, w, h);
            } else {
                drawBall(canvas, opt, w, h);
            }
        }

        if (celebrating) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10f);
            paint.setColor(Color.parseColor("#2EC4B6"));
            canvas.drawCircle(celebrateCx, celebrateCy, celebrateRadius, paint);
        }
    }

    private void drawBackground(Canvas canvas, int w, int h) {
        float skyBottom = h * 0.58f;
        float seaBottom = h * 0.78f;

        // Sky gradient: warm orange near the horizon fading to a soft blue up top
        paint.setShader(new LinearGradient(0, 0, 0, skyBottom,
                Color.parseColor("#4FC3E8"), Color.parseColor("#FFD98A"), Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, w, skyBottom, paint);
        paint.setShader(null);

        // Sun
        paint.setColor(Color.parseColor("#FFEB6B"));
        canvas.drawCircle(w * 0.82f, h * 0.14f, Math.min(w, h) * 0.075f, paint);

        // Clouds
        drawCloud(canvas, w * 0.18f, h * 0.10f, Math.min(w, h) * 0.045f);
        drawCloud(canvas, w * 0.45f, h * 0.06f, Math.min(w, h) * 0.032f);

        // Sea
        paint.setShader(new LinearGradient(0, skyBottom, 0, seaBottom,
                Color.parseColor("#1AAFC9"), Color.parseColor("#0E8FAE"), Shader.TileMode.CLAMP));
        canvas.drawRect(0, skyBottom, w, seaBottom, paint);
        paint.setShader(null);

        // Wave lines on the sea
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.parseColor("#66FFFFFF"));
        for (int i = 0; i < 3; i++) {
            float waveY = skyBottom + (seaBottom - skyBottom) * (0.3f + i * 0.28f);
            Path wave = new Path();
            wave.moveTo(0, waveY);
            float step = w / 6f;
            for (int seg = 0; seg < 6; seg++) {
                float midX = step * seg + step / 2f;
                float dy = (seg % 2 == 0) ? -8f : 8f;
                wave.quadTo(midX, waveY + dy, step * (seg + 1), waveY);
            }
            canvas.drawPath(wave, paint);
        }

        // Sand, with a slightly darker waterline edge
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E0B579"));
        canvas.drawRect(0, seaBottom, w, seaBottom + h * 0.02f, paint);
        paint.setColor(Color.parseColor("#F4D9A0"));
        canvas.drawRect(0, seaBottom + h * 0.02f, w, h, paint);

        // Scattered shell/speckle dots on the sand
        paint.setColor(Color.parseColor("#33805030"));
        float sandH = h - seaBottom;
        canvas.drawCircle(w * 0.10f, seaBottom + sandH * 0.5f, 5f, paint);
        canvas.drawCircle(w * 0.30f, seaBottom + sandH * 0.75f, 4f, paint);
        canvas.drawCircle(w * 0.60f, seaBottom + sandH * 0.4f, 5f, paint);
        canvas.drawCircle(w * 0.88f, seaBottom + sandH * 0.65f, 4f, paint);

        // Simple palm tree silhouette in the bottom-left corner
        drawPalmTree(canvas, w * 0.06f, h, Math.min(w, h) * 0.22f);
    }

    private void drawCloud(Canvas canvas, float cx, float cy, float r) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#CCFFFFFF"));
        canvas.drawCircle(cx, cy, r, paint);
        canvas.drawCircle(cx + r * 0.9f, cy + r * 0.15f, r * 0.75f, paint);
        canvas.drawCircle(cx - r * 0.9f, cy + r * 0.15f, r * 0.65f, paint);
    }

    private void drawPalmTree(Canvas canvas, float baseX, float baseY, float height) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(height * 0.09f);
        paint.setColor(Color.parseColor("#5B3A21"));
        Path trunk = new Path();
        trunk.moveTo(baseX, baseY);
        trunk.quadTo(baseX + height * 0.15f, baseY - height * 0.55f, baseX + height * 0.05f, baseY - height * 0.85f);
        canvas.drawPath(trunk, paint);

        float topX = baseX + height * 0.05f;
        float topY = baseY - height * 0.85f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#2E8B4F"));
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(-90 + (i - 2) * 32);
            float leafLen = height * 0.42f;
            Path leaf = new Path();
            leaf.moveTo(topX, topY);
            float endX = topX + (float) Math.cos(angle) * leafLen;
            float endY = topY + (float) Math.sin(angle) * leafLen;
            float ctrlX = topX + (float) Math.cos(angle - 0.3) * leafLen * 0.6f;
            float ctrlY = topY + (float) Math.sin(angle - 0.3) * leafLen * 0.6f;
            leaf.quadTo(ctrlX, ctrlY, endX, endY);
            float ctrl2X = topX + (float) Math.cos(angle + 0.15) * leafLen * 0.5f;
            float ctrl2Y = topY + (float) Math.sin(angle + 0.15) * leafLen * 0.5f;
            leaf.quadTo(ctrl2X, ctrl2Y, topX, topY);
            canvas.drawPath(leaf, paint);
        }
    }

    private void drawBall(Canvas canvas, Option opt, int w, int h) {
        float cx = opt.cx * w;
        float cy = opt.cy * h;
        float r = opt.radius * Math.min(w, h);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(opt.color);
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.parseColor("#33000000"));
        canvas.drawCircle(cx, cy, r, paint);

        drawLabel(canvas, opt.label, cx, cy, r);
    }

    private void drawLabel(Canvas canvas, String label, float cx, float cy, float r) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#26333F"));
        float textSize = r * 0.30f;
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);

        while (paint.measureText(label) > r * 1.7f && textSize > 10f) {
            textSize -= 1f;
            paint.setTextSize(textSize);
        }

        canvas.drawText(label, cx, cy + textSize * 0.35f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawPop(Canvas canvas, Option opt, int w, int h) {
        float cx = opt.cx * w;
        float cy = opt.cy * h;
        float baseR = opt.radius * Math.min(w, h);

        float coreR = baseR * (1f - popProgress);
        int alpha = (int) (255 * (1f - popProgress));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(opt.color);
        paint.setAlpha(alpha);
        if (coreR > 0) canvas.drawCircle(cx, cy, coreR, paint);

        int particles = 8;
        float dist = popProgress * baseR * 2.2f;
        float pieceR = baseR * 0.18f * (1f - popProgress * 0.6f);
        paint.setAlpha(alpha);
        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI / particles) * i;
            float px = cx + (float) Math.cos(angle) * dist;
            float py = cy + (float) Math.sin(angle) * dist;
            canvas.drawCircle(px, py, Math.max(pieceR, 0f), paint);
        }
        paint.setAlpha(255);
    }

    // ---------- Input ----------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (inputLocked || activeQuestions == null) return true;
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        int w = getWidth();
        int h = getHeight();
        float px = event.getX();
        float py = event.getY();

        Question q = activeQuestions.get(stageIndex);
        for (Option opt : q.options) {
            if (opt.contains(px / w, py / h, w, h)) {
                boolean correct = opt.id.equals(q.correctId);
                inputLocked = true;
                if (correct) {
                    score++;
                    playCelebration(opt.cx * w, opt.cy * h);
                } else {
                    lives--;
                    playPop(opt.id);
                }
                if (listener != null) {
                    listener.onAnswer(correct, lives, score);
                }
                postDelayed(() -> resolveAfterAnswer(correct), correct ? 650 : 550);
                return true;
            }
        }
        return true;
    }

    private void resolveAfterAnswer(boolean lastWasCorrect) {
        poppingOptionId = null;
        celebrating = false;

        if (lastWasCorrect) {
            stageIndex++;
            if (stageIndex >= activeQuestions.size()) {
                if (listener != null) listener.onGameOver(true, activeQuestions.size(), score, activeQuestions.size());
                return;
            }
        } else if (lives <= 0) {
            if (listener != null) listener.onGameOver(false, stageIndex + 1, score, activeQuestions.size());
            return;
        }

        inputLocked = false;
        invalidate();
    }

    // ---------- Feedback animations ----------

    private void playPop(String optionId) {
        poppingOptionId = optionId;
        popProgress = 0f;
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(400);
        anim.addUpdateListener(a -> {
            popProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.start();

        ValueAnimator shake = ValueAnimator.ofFloat(-14f, 14f);
        shake.setDuration(55);
        shake.setRepeatCount(4);
        shake.setRepeatMode(ValueAnimator.REVERSE);
        shake.addUpdateListener(a -> setTranslationX((float) a.getAnimatedValue()));
        shake.start();
    }

    private void playCelebration(float x, float y) {
        celebrating = true;
        celebrateCx = x;
        celebrateCy = y;
        ValueAnimator anim = ValueAnimator.ofFloat(20f, 100f);
        anim.setDuration(300);
        anim.addUpdateListener(a -> {
            celebrateRadius = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.start();
    }
}