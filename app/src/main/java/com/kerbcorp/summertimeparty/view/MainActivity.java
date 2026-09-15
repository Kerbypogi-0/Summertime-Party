package com.kerbcorp.summertimeparty.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.kerbcorp.summertimeparty.R;
import com.kerbcorp.summertimeparty.viewmodel.DashboardViewModel;

public class MainActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide headers for full screen
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // 1. Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // 2. Observe the ViewModel for Navigation Commands
        viewModel.getNavigateToGame().observe(this, gameActivityClass -> {
            if (gameActivityClass != null) {
                Intent intent = new Intent(MainActivity.this, gameActivityClass);
                startActivity(intent);
                viewModel.onNavigationComplete(); // Clear the state
            }
        });

        // 3. Connect UI buttons to ViewModel actions (No Intent logic here anymore!)
        findViewById(R.id.btnSnake).setOnClickListener(v -> viewModel.onSnakeClicked());
        findViewById(R.id.btnPig).setOnClickListener(v -> viewModel.onPigClicked());
        findViewById(R.id.btnGoSki).setOnClickListener(v -> viewModel.onGoSkiClicked());
        findViewById(R.id.btnCessBomb).setOnClickListener(v -> viewModel.onCessBombClicked());
        findViewById(R.id.btnMatching).setOnClickListener(v -> viewModel.onMatchingClicked());
    }
}