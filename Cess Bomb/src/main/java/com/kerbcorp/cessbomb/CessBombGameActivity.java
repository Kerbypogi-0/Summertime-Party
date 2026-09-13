package com.kerbcorp.cessbomb;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Core gameplay Activity for CessBomb.
 * Runs a single countdown timer for the whole match, presents a queue of
 * randomized challenges, then a final defusal step.
 */
public class CessBombGameActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "cessbomb_extra_difficulty";

    private Difficulty difficulty;
    private ChallengeManager challengeManager;
    private CountDownTimer countDownTimer;
    private long timeLeftMillis;

    private int score = 0;
    private Challenge currentChallenge;

    // Memory challenge state
    private final List<String> memoryPlayerInput = new ArrayList<>();
    private List<String> memoryTargetSequence;
    private boolean memoryRevealInProgress = false;

    // Top bar views
    private TextView textTimeLeft;
    private TextView textScore;
    private TextView textChallengeProgress;

    // Challenge containers
    private LinearLayout layoutButtonChallenge;
    private LinearLayout layoutWireChallenge;
    private LinearLayout layoutCodeChallenge;
    private LinearLayout layoutMemoryChallenge;
    private LinearLayout layoutFinalDefuse;

    private TextView textButtonPrompt;
    private LinearLayout buttonOptionsContainer;

    private TextView textWirePrompt;
    private LinearLayout wireOptionsContainer;

    private TextView textCodeEquation1;
    private TextView textCodeEquation2;
    private TextView textCodeQuestion;
    private EditText inputCodeAnswer;
    private Button submitCodeAnswer;

    private TextView textMemoryPrompt;
    private LinearLayout memorySequenceDisplay;
    private LinearLayout memoryIconChoices;
    private TextView textMemoryPlayerInput;
    private Button clearMemoryInput;

    private Button btnDefuse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cessbomb_game);

        String difficultyName = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        difficulty = difficultyName != null ? Difficulty.valueOf(difficultyName) : Difficulty.NORMAL;
        challengeManager = new ChallengeManager(difficulty);

        bindViews();
        startTimer();
        loadNextChallenge();
    }

    private void bindViews() {
        textTimeLeft = findViewById(R.id.textTimeLeft);
        textScore = findViewById(R.id.textScore);
        textChallengeProgress = findViewById(R.id.textChallengeProgress);

        layoutButtonChallenge = findViewById(R.id.layoutButtonChallenge);
        layoutWireChallenge = findViewById(R.id.layoutWireChallenge);
        layoutCodeChallenge = findViewById(R.id.layoutCodeChallenge);
        layoutMemoryChallenge = findViewById(R.id.layoutMemoryChallenge);
        layoutFinalDefuse = findViewById(R.id.layoutFinalDefuse);

        textButtonPrompt = findViewById(R.id.textButtonPrompt);
        buttonOptionsContainer = findViewById(R.id.buttonOptionsContainer);

        textWirePrompt = findViewById(R.id.textWirePrompt);
        wireOptionsContainer = findViewById(R.id.wireOptionsContainer);

        textCodeEquation1 = findViewById(R.id.textCodeEquation1);
        textCodeEquation2 = findViewById(R.id.textCodeEquation2);
        textCodeQuestion = findViewById(R.id.textCodeQuestion);
        inputCodeAnswer = findViewById(R.id.inputCodeAnswer);
        submitCodeAnswer = findViewById(R.id.submitCodeAnswer);

        textMemoryPrompt = findViewById(R.id.textMemoryPrompt);
        memorySequenceDisplay = findViewById(R.id.memorySequenceDisplay);
        memoryIconChoices = findViewById(R.id.memoryIconChoices);
        textMemoryPlayerInput = findViewById(R.id.textMemoryPlayerInput);
        clearMemoryInput = findViewById(R.id.clearMemoryInput);

        btnDefuse = findViewById(R.id.btnDefuse);
        btnDefuse.setOnClickListener(v -> endGame(true));
    }

    // ---------- Timer ----------

    private void startTimer() {
        long totalMillis = difficulty.timeSeconds * 1000L;
        timeLeftMillis = totalMillis;
        countDownTimer = new CountDownTimer(totalMillis, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimeDisplay();
            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateTimeDisplay();
                endGame(false);
            }
        };
        countDownTimer.start();
    }

    private void updateTimeDisplay() {
        int secondsLeft = (int) Math.ceil(timeLeftMillis / 1000.0);
        textTimeLeft.setText(getString(R.string.cessbomb_time_left_format, secondsLeft));
    }

    // ---------- Challenge flow ----------

    private void loadNextChallenge() {
        hideAllChallengeLayouts();
        if (challengeManager.hasNext()) {
            currentChallenge = challengeManager.next();
            textChallengeProgress.setText(getString(R.string.cessbomb_challenge_progress_format,
                    challengeManager.getCompletedCount(), challengeManager.getTotalCount()));

            switch (currentChallenge.getType()) {
                case BUTTON:
                    showButtonChallenge((ButtonChallenge) currentChallenge);
                    break;
                case WIRE:
                    showWireChallenge((WireChallenge) currentChallenge);
                    break;
                case CODE:
                    showCodeChallenge((CodeChallenge) currentChallenge);
                    break;
                case MEMORY:
                    showMemoryChallenge((MemoryChallenge) currentChallenge);
                    break;
            }
        } else {
            showFinalDefuse();
        }
    }

    private void hideAllChallengeLayouts() {
        layoutButtonChallenge.setVisibility(View.GONE);
        layoutWireChallenge.setVisibility(View.GONE);
        layoutCodeChallenge.setVisibility(View.GONE);
        layoutMemoryChallenge.setVisibility(View.GONE);
        layoutFinalDefuse.setVisibility(View.GONE);
    }

    // ---------- Button challenge ----------

    private void showButtonChallenge(ButtonChallenge challenge) {
        layoutButtonChallenge.setVisibility(View.VISIBLE);
        textButtonPrompt.setText(challenge.getPromptText());
        buttonOptionsContainer.removeAllViews();

        for (int i = 0; i < challenge.options.size(); i++) {
            ButtonChallenge.Option option = challenge.options.get(i);
            Button button = new Button(this);
            button.setText(option.label);
            button.setBackgroundResource(R.drawable.cessbomb_button_bg);
            button.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 12, 0, 0);
            button.setLayoutParams(params);

            final int index = i;
            button.setOnClickListener(v -> {
                if (challenge.checkAnswer(index)) {
                    onChallengeCorrect();
                } else {
                    onChallengeWrong(button);
                }
            });
            buttonOptionsContainer.addView(button);
        }
    }

    // ---------- Wire challenge ----------

    private void showWireChallenge(WireChallenge challenge) {
        layoutWireChallenge.setVisibility(View.VISIBLE);
        textWirePrompt.setText(challenge.getPromptText());
        wireOptionsContainer.removeAllViews();

        for (int i = 0; i < challenge.wires.size(); i++) {
            WireChallenge.WireColor wire = challenge.wires.get(i);
            View wireView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 60);
            params.setMargins(0, 12, 0, 0);
            wireView.setLayoutParams(params);
            wireView.setBackgroundColor(wire.colorHex);

            final int index = i;
            wireView.setOnClickListener(v -> {
                if (challenge.checkAnswer(index)) {
                    onChallengeCorrect();
                } else {
                    onChallengeWrong(wireView);
                }
            });
            wireOptionsContainer.addView(wireView);
        }
    }

    // ---------- Code challenge ----------

    private void showCodeChallenge(CodeChallenge challenge) {
        layoutCodeChallenge.setVisibility(View.VISIBLE);
        textCodeEquation1.setText(challenge.equationLine1);
        textCodeEquation2.setText(challenge.equationLine2);
        textCodeQuestion.setText(challenge.questionLine);
        inputCodeAnswer.setText("");

        submitCodeAnswer.setOnClickListener(v -> {
            String raw = inputCodeAnswer.getText().toString().trim();
            if (TextUtils.isEmpty(raw)) {
                Toast.makeText(this, R.string.cessbomb_enter_a_number, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int entered = Integer.parseInt(raw);
                if (challenge.checkAnswer(entered)) {
                    onChallengeCorrect();
                } else {
                    onChallengeWrong(inputCodeAnswer);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.cessbomb_enter_a_number, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- Memory challenge ----------

    private void showMemoryChallenge(MemoryChallenge challenge) {
        layoutMemoryChallenge.setVisibility(View.VISIBLE);
        textMemoryPrompt.setText(challenge.getPromptText());
        memoryTargetSequence = challenge.sequence;
        memoryPlayerInput.clear();
        textMemoryPlayerInput.setText("");

        memoryIconChoices.setVisibility(View.INVISIBLE);
        memorySequenceDisplay.removeAllViews();
        for (String icon : memoryTargetSequence) {
            memorySequenceDisplay.addView(makeIconTextView(icon));
        }

        memoryRevealInProgress = true;
        // Hide the sequence after a short delay, then reveal the tap choices.
        long revealMillis = 1200 + (memoryTargetSequence.size() * 500L);
        memorySequenceDisplay.postDelayed(() -> {
            memorySequenceDisplay.removeAllViews();
            memoryRevealInProgress = false;
            setupMemoryChoices();
            memoryIconChoices.setVisibility(View.VISIBLE);
        }, revealMillis);

        clearMemoryInput.setOnClickListener(v -> {
            memoryPlayerInput.clear();
            textMemoryPlayerInput.setText("");
        });
    }

    private void setupMemoryChoices() {
        memoryIconChoices.removeAllViews();
        for (String icon : MemoryChallenge.ICON_POOL) {
            TextView iconView = makeIconTextView(icon);
            iconView.setClickable(true);
            iconView.setOnClickListener(v -> onMemoryIconTapped(icon));
            memoryIconChoices.addView(iconView);
        }
    }

    private void onMemoryIconTapped(String icon) {
        if (memoryRevealInProgress || memoryTargetSequence == null) return;
        memoryPlayerInput.add(icon);
        textMemoryPlayerInput.setText(TextUtils.join(" ", memoryPlayerInput));

        if (memoryPlayerInput.size() == memoryTargetSequence.size()) {
            MemoryChallenge memoryChallenge = (MemoryChallenge) currentChallenge;
            if (memoryChallenge.checkAnswer(memoryPlayerInput)) {
                onChallengeCorrect();
            } else {
                onChallengeWrong(textMemoryPlayerInput);
                // Give the player a fresh sequence to retry.
                memoryPlayerInput.clear();
                textMemoryPlayerInput.setText("");
                showMemoryChallenge(MemoryChallenge.generate(difficulty, new java.util.Random()));
            }
        }
    }

    private TextView makeIconTextView(String icon) {
        TextView tv = new TextView(this);
        tv.setText(icon);
        tv.setTextSize(32);
        tv.setPadding(16, 16, 16, 16);
        return tv;
    }

    // ---------- Final defusal ----------

    private void showFinalDefuse() {
        layoutFinalDefuse.setVisibility(View.VISIBLE);
        textChallengeProgress.setText(getString(R.string.cessbomb_ready_to_defuse));
    }

    // ---------- Shared correct/wrong handling ----------

    private void onChallengeCorrect() {
        int timeBonus = (int) (timeLeftMillis / 1000L) >= (difficulty.timeSeconds / 2) ? 25 : 0;
        score += 100 + timeBonus;
        updateScoreDisplay();
        Toast.makeText(this, R.string.cessbomb_correct, Toast.LENGTH_SHORT).show();
        loadNextChallenge();
    }

    private void onChallengeWrong(View source) {
        score = Math.max(0, score - 50);
        updateScoreDisplay();
        source.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cessbomb_shake));
        Toast.makeText(this, R.string.cessbomb_wrong, Toast.LENGTH_SHORT).show();
    }

    private void updateScoreDisplay() {
        textScore.setText(getString(R.string.cessbomb_score_format, score));
    }

    // ---------- End of game ----------

    private void endGame(boolean win) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (win) {
            score += 500;
        }

        int timeRemainingSeconds = (int) (timeLeftMillis / 1000L);
        int challengesCompleted = challengeManager.getCompletedCount();
        int totalChallenges = challengeManager.getTotalCount();

        CessBombScoreManager scoreManager = new CessBombScoreManager(this);
        boolean isNewBest = scoreManager.saveScoreIfBest(score);
        int bestScore = scoreManager.getBestScore();

        Intent intent = new Intent(this, CessBombResultActivity.class);
        intent.putExtra(CessBombResultActivity.EXTRA_WIN, win);
        intent.putExtra(CessBombResultActivity.EXTRA_SCORE, score);
        intent.putExtra(CessBombResultActivity.EXTRA_DIFFICULTY, difficulty.name());
        intent.putExtra(CessBombResultActivity.EXTRA_TIME_REMAINING, timeRemainingSeconds);
        intent.putExtra(CessBombResultActivity.EXTRA_CHALLENGES_COMPLETED, challengesCompleted);
        intent.putExtra(CessBombResultActivity.EXTRA_TOTAL_CHALLENGES, totalChallenges);
        intent.putExtra(CessBombResultActivity.EXTRA_BEST_SCORE, bestScore);
        intent.putExtra(CessBombResultActivity.EXTRA_NEW_BEST, isNewBest);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
