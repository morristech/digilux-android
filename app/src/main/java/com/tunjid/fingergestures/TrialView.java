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


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tunjid.fingergestures.billing.PurchasesManager;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

import static android.view.Window.FEATURE_OPTIONS_PANEL;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

@SuppressLint("ViewConstructor")
public class TrialView extends FrameLayout {

    private TextView textView;
    private ImageView imageView;
    private Disposable disposable;

    public TrialView(@NonNull Context context, MenuItem menuItem) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.trial_view, this, true);

        textView = root.findViewById(R.id.text);
        imageView = root.findViewById(R.id.icon);

        OnClickListener clickListener = view -> {
            Activity activity = getActivity(getContext());
            if (activity != null) activity.onMenuItemSelected(FEATURE_OPTIONS_PANEL, menuItem);
        };

        setOnClickListener(clickListener);
        imageView.setOnClickListener(clickListener);

        Flowable<Long> flowable = PurchasesManager.getInstance().getTrialFlowable();

        if (flowable == null) changeState(false);
        else disposable = flowable.map(String::valueOf)
                .doOnSubscribe(subscription -> changeState(true))
                .doOnComplete(() -> {
                    Activity activity = getActivity(getContext());
                    if (activity == null) return;

                    activity.runOnUiThread(() -> {
                        changeState(false);
                        activity.recreate();
                    });
                })
                .observeOn(mainThread())
                .subscribe(textView::setText, Throwable::printStackTrace);

        CheatSheet.setup(this, menuItem.getTitle());
    }

    @Override
    protected void onDetachedFromWindow() {
        if (disposable != null) disposable.dispose();
        super.onDetachedFromWindow();
    }

    private void changeState(boolean isOnTrial) {
        imageView.setVisibility(isOnTrial ? GONE : VISIBLE);
        textView.setVisibility(isOnTrial ? VISIBLE : GONE);
    }

    @Nullable
    private Activity getActivity(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) return (Activity) context;
        return null;
    }
}
