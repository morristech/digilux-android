package com.tunjid.fingergestures;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.tunjid.fingergestures.billing.PurchasesManager;
import com.tunjid.fingergestures.gestureconsumers.GestureConsumer;

import java.util.List;

public class PopUpGestureConsumer implements GestureConsumer {

    public static final String ACTION_ACCESSIBILITY_BUTTON = "com.tunjid.fingergestures.action.accessibilityButton";
    public static final String ACTION_SHOW_POPUP= "PopUpGestureConsumer shows popup";
    public static final String EXTRA_SHOWS_ACCESSIBILITY_BUTTON = "extra shows accessibility button";
    private static final String ACCESSIBILITY_BUTTON_ENABLED = "accessibility button enabled";
    private static final String SAVED_ACTIONS = "accessibility button apps";
    private static final String ANIMATES_POPUP = "animates popup";

    private final SetManager<Integer> setManager;

    private static PopUpGestureConsumer instance;

    public static PopUpGestureConsumer getInstance() {
        if (instance == null) instance = new PopUpGestureConsumer();
        return instance;
    }

    @Override
    public void onGestureActionTriggered(int gestureAction) {
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(new Intent(ACTION_SHOW_POPUP));
    }

    @Override
    public boolean accepts(int gesture) {
        return gesture == SHOW_POPUP;
    }

    private PopUpGestureConsumer() {
        setManager = new SetManager<>(Integer::compare, this::canAddToSet, Integer::valueOf, String::valueOf);
    }

    public boolean hasAccessibilityButton() {
        return App.getInstance().getPreferences().getBoolean(ACCESSIBILITY_BUTTON_ENABLED, false);
    }

    public boolean shouldAnimatePopup() {
        App app = App.getInstance();
        return app != null && app.getPreferences().getBoolean(ANIMATES_POPUP, true);
    }

    public boolean addToSet(@GestureConsumer.GestureAction int action) {
        return setManager.addToSet(String.valueOf(action), SAVED_ACTIONS);
    }

    public void removeFromSet(@GestureConsumer.GestureAction int action) {
        setManager.removeFromSet(String.valueOf(action), SAVED_ACTIONS);
    }

    public void setAnimatesPopup(boolean visible) {
        App app = App.getInstance();
        if (app != null) app.getPreferences().edit().putBoolean(ANIMATES_POPUP, visible).apply();
    }

    public List<String> getList() {
        return setManager.getList(SAVED_ACTIONS);
    }

    public void enableAccessibilityButton(boolean enabled) {
        App.getInstance().getPreferences().edit().putBoolean(ACCESSIBILITY_BUTTON_ENABLED, enabled).apply();

        Intent intent = new Intent(ACTION_ACCESSIBILITY_BUTTON);
        intent.putExtra(EXTRA_SHOWS_ACCESSIBILITY_BUTTON, enabled);

        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
    }

    private boolean canAddToSet(String preferenceName) {
        return setManager.getSet(preferenceName).size() < 2 || PurchasesManager.getInstance().isPremiumNotTrial();
    }
}