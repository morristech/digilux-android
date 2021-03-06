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

package com.tunjid.fingergestures.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.recyclerview.ListManager;
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder;
import com.tunjid.fingergestures.PopUpGestureConsumer;
import com.tunjid.fingergestures.R;
import com.tunjid.fingergestures.adapters.ActionAdapter;
import com.tunjid.fingergestures.baseclasses.MainActivityFragment;
import com.tunjid.fingergestures.billing.PurchasesManager;
import com.tunjid.fingergestures.gestureconsumers.GestureConsumer;
import com.tunjid.fingergestures.gestureconsumers.GestureMapper;
import com.tunjid.fingergestures.viewholders.ActionViewHolder;
import com.tunjid.fingergestures.viewmodels.AppViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.DOUBLE_DOWN_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.DOUBLE_LEFT_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.DOUBLE_RIGHT_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.DOUBLE_UP_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.DOWN_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.LEFT_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.RIGHT_GESTURE;
import static com.tunjid.fingergestures.gestureconsumers.GestureMapper.UP_GESTURE;
import static com.tunjid.fingergestures.viewmodels.AppViewModel.MAP_DOWN_ICON;
import static com.tunjid.fingergestures.viewmodels.AppViewModel.MAP_LEFT_ICON;
import static com.tunjid.fingergestures.viewmodels.AppViewModel.MAP_RIGHT_ICON;
import static com.tunjid.fingergestures.viewmodels.AppViewModel.MAP_UP_ICON;
import static com.tunjid.fingergestures.viewmodels.AppViewModel.POPUP_ACTION;

public class ActionFragment extends MainActivityFragment implements ActionAdapter.ActionClickListener {

    private static final String ARG_DIRECTION = "DIRECTION";

    private AppViewModel viewModel;

    public static ActionFragment directionInstance(@GestureMapper.GestureDirection String direction) {
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();

        args.putString(ARG_DIRECTION, direction);
        fragment.setArguments(args);
        return fragment;
    }

    public static ActionFragment popUpInstance() {
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(requireActivity()).get(AppViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_actions, container, false);

        root.<Toolbar>findViewById(R.id.title_bar).setTitle(R.string.pick_action);
        ListManager<ActionViewHolder, Void> listManager =  new ListManagerBuilder<ActionViewHolder, Void>()
                .withRecyclerView(root.findViewById(R.id.options_list))
                .withLinearLayoutManager()
                .withAdapter(new ActionAdapter(false, true, viewModel.state.availableActions, this))
                .addDecoration(divider())
                .build();

        disposables.add(viewModel.updatedActions().subscribe(listManager::onDiff, Throwable::printStackTrace));

        return root;
    }

    @Override
    public void onActionClicked(@GestureConsumer.GestureAction int action) {
        Bundle args = getArguments();
        if (args == null) {
            showSnackbar(R.string.generic_error);
            return;
        }

        @GestureMapper.GestureDirection
        String direction = args.getString(ARG_DIRECTION);

        boolean isPopUpInstance = direction == null;

        toggleBottomSheet(false);

        AppFragment fragment = getCurrentAppFragment();
        if (fragment == null) return;

        GestureMapper mapper = GestureMapper.getInstance();

        if (isPopUpInstance) {
            Context context = requireContext();
            if (PopUpGestureConsumer.getInstance().addToSet(action))
                fragment.notifyItemChanged(POPUP_ACTION);
            else new AlertDialog.Builder(context)
                    .setTitle(R.string.go_premium_title)
                    .setMessage(context.getString(R.string.go_premium_body, context.getString(R.string.popup_description)))
                    .setPositiveButton(R.string.continue_text, (dialog, which) -> purchase(PurchasesManager.PREMIUM_SKU))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        }
        else {
            mapper.mapGestureToAction(direction, action);
            fragment.notifyItemChanged(LEFT_GESTURE.equals(direction) || DOUBLE_LEFT_GESTURE.equals(direction)
                    ? MAP_LEFT_ICON
                    : UP_GESTURE.equals(direction) || DOUBLE_UP_GESTURE.equals(direction)
                    ? MAP_UP_ICON
                    : RIGHT_GESTURE.equals(direction) || DOUBLE_RIGHT_GESTURE.equals(direction)
                    ? MAP_RIGHT_ICON
                    : DOWN_GESTURE.equals(direction) || DOUBLE_DOWN_GESTURE.equals(direction)
                    ? MAP_DOWN_ICON : MAP_DOWN_ICON);
        }
    }
}
