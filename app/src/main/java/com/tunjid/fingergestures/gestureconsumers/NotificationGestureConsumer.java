package com.tunjid.fingergestures.gestureconsumers;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.tunjid.fingergestures.App.withApp;

public class NotificationGestureConsumer implements GestureConsumer {

    public static final String ACTION_NOTIFICATION_UP = "NotificationGestureConsumer up";
    public static final String ACTION_NOTIFICATION_DOWN = "NotificationGestureConsumer down";
    public static final String ACTION_NOTIFICATION_TOGGLE = "NotificationGestureConsumer toggle";

    private static NotificationGestureConsumer instance;

    static NotificationGestureConsumer getInstance() {
        if (instance == null) instance = new NotificationGestureConsumer();
        return instance;
    }

    private NotificationGestureConsumer() {}

    @Override
    @SuppressLint("SwitchIntDef")
    public boolean accepts(@GestureAction int gesture) {
        switch (gesture) {
            case NOTIFICATION_UP:
            case NOTIFICATION_DOWN:
            case NOTIFICATION_TOGGLE:
                return true;
            default:
                return false;
        }
    }

    @Override
    @SuppressLint("SwitchIntDef")
    public void onGestureActionTriggered(@GestureAction int gestureAction) {
        switch (gestureAction) {
            case NOTIFICATION_UP:
            case NOTIFICATION_DOWN:
            case NOTIFICATION_TOGGLE:
                withApp(app -> LocalBroadcastManager.getInstance(app)
                        .sendBroadcast(new Intent(gestureAction == NOTIFICATION_UP
                                ? ACTION_NOTIFICATION_UP
                                : gestureAction == NOTIFICATION_DOWN
                                ? ACTION_NOTIFICATION_DOWN
                                : ACTION_NOTIFICATION_TOGGLE)));
                break;
        }
    }
}

