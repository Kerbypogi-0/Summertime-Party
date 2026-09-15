package com.kerbcorp.summertimeparty.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kerbcorp.catchthatpig.CatchThatPigActivity;
import com.kerbcorp.cessbomb.CessBombActivity;
import com.kerbcorp.goski.GoSkiActivity;
import com.kerbcorp.matchinggame.MatchingGameActivity;
import com.kerbcorp.snakeandapple.SnakeAndAppleActivity;

public class DashboardViewModel extends ViewModel {

    // LiveData acts as a messenger to tell the View (MainActivity) what to do
    private final MutableLiveData<Class<?>> navigateToGame = new MutableLiveData<>();

    public LiveData<Class<?>> getNavigateToGame() {
        return navigateToGame;
    }

    // These methods handle the logic when a button is clicked
    public void onSnakeClicked() { navigateToGame.setValue(SnakeAndAppleActivity.class); }
    public void onPigClicked() { navigateToGame.setValue(CatchThatPigActivity.class); }
    public void onGoSkiClicked() { navigateToGame.setValue(GoSkiActivity.class); }
    public void onCessBombClicked() { navigateToGame.setValue(CessBombActivity.class); }
    public void onMatchingClicked() { navigateToGame.setValue(MatchingGameActivity.class); }

    // Reset the navigation state so it doesn't trigger twice
    public void onNavigationComplete() { navigateToGame.setValue(null); }
}