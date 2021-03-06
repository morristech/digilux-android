/*
 * Copyright (c) 2017, 2018, 2019 Adetunji Dahunsi.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tunjid.fingergestures;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Diff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.accessibility.AccessibilityEvent.TYPES_ALL_MASK;
import static io.reactivex.Flowable.timer;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class App extends android.app.Application {

    private static final String BRIGHTNESS_PREFS = "brightness prefs";
    public static final String EMPTY = "";

    private static App instance;
    private final PublishProcessor<Intent> broadcaster = PublishProcessor.create();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public SharedPreferences getPreferences() {
        return getSharedPreferences(BRIGHTNESS_PREFS, MODE_PRIVATE);
    }

    public void broadcast(Intent intent) {
        broadcaster.onNext(intent);
    }

    // Wrap the subject so if there's an error downstream, it doesn't propagate back up to it.
    // This way, the broadcast stream should never error or terminate
    public Flowable<Intent> broadcasts() {
        return Flowable.defer(() -> broadcaster).onErrorResumeNext(this::logAndResume);
    }

    // Log the error, and re-wrap the broadcast processor
    private Flowable<Intent> logAndResume(Throwable throwable) {
        Log.e("App Broadcasts", "Error in broadcast stream", throwable);
        return broadcasts();
    }

    @Nullable
    public static App getInstance() {
        return instance;
    }

    @NonNull
    public static Intent settingsIntent() {
        return new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + instance.getPackageName()));
    }

    @NonNull
    public static Intent accessibilityIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    @NonNull
    public static Intent doNotDisturbIntent() {
        return new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
    }

    public static Disposable delay(long interval, TimeUnit timeUnit, Runnable runnable) {
        return timer(interval, timeUnit).subscribe(ignored -> runnable.run(), Throwable::printStackTrace);
    }

    public static boolean canWriteToSettings() {
        return transformApp(Settings.System::canWrite, false);
    }

    public static boolean hasStoragePermission() {
        return transformApp(app -> ContextCompat.checkSelfPermission(app, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED, false);
    }

    public static boolean isPieOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static boolean hasDoNotDisturbAccess() {
        return transformApp(app -> {
            NotificationManager notificationManager = app.getSystemService(NotificationManager.class);
            return notificationManager != null && notificationManager.isNotificationPolicyAccessGranted();
        }, false);
    }

    public static boolean accessibilityServiceEnabled() {
        return transformApp(app -> {
            String key = app.getPackageName();

            AccessibilityManager accessibilityManager = ((AccessibilityManager) app.getSystemService(ACCESSIBILITY_SERVICE));
            if (accessibilityManager == null) return false;

            List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(TYPES_ALL_MASK);

            for (AccessibilityServiceInfo info : list) if (info.getId().contains(key)) return true;
            return false;
        }, false);
    }

    public static void withApp(Consumer<App> appConsumer) {
        App app = getInstance();
        if (app == null) return;

        appConsumer.accept(app);
    }

    public static <T> T transformApp(Function<App, T> appTFunction, T defaultValue) {
        App app = getInstance();
        return app != null ? appTFunction.apply(app) : defaultValue;
    }

    @Nullable
    public static <T> T transformApp(Function<App, T> appTFunction) {
        return transformApp(appTFunction, null);
    }

    public static <T> void nullCheck(@Nullable T target, Consumer<T> consumer) {
        if (target != null) consumer.accept(target);
    }

    public static <T> Single<DiffUtil.DiffResult> diff(List<T> list, Supplier<List<T>> supplier) {
        return diff(list, supplier, Object::toString);
    }

    public static <T> Single<DiffUtil.DiffResult> diff(List<T> list,
                                                       Supplier<List<T>> supplier,
                                                       Function<T, String> diffFunction) {
        return backgroundToMain(() -> Diff.calculate(
                list,
                supplier.get(),
                (listCopy, newList) -> newList,
                item -> Differentiable.fromCharSequence(() -> diffFunction.apply(item))))
                .doOnSuccess(diff -> Lists.replace(list, diff.items))
                .map(diff -> diff.result);
    }

    public static <T> Single<T> backgroundToMain(Supplier<T> supplier) {
        return Single.fromCallable(supplier::get)
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread());
    }
}
