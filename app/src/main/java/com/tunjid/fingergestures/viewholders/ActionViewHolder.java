package com.tunjid.fingergestures.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;
import com.tunjid.fingergestures.BackgroundManager;
import com.tunjid.fingergestures.R;
import com.tunjid.fingergestures.adapters.ActionAdapter;
import com.tunjid.fingergestures.gestureconsumers.BrightnessGestureConsumer;
import com.tunjid.fingergestures.gestureconsumers.GestureConsumer;
import com.tunjid.fingergestures.gestureconsumers.GestureMapper;

import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.DO_NOTHING;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.INCREASE_BRIGHTNESS;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.MAXIMIZE_BRIGHTNESS;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.MINIMIZE_BRIGHTNESS;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.NOTIFICATION_DOWN;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.NOTIFICATION_UP;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.REDUCE_BRIGHTNESS;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.TOGGLE_AUTO_ROTATE;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.TOGGLE_DOCK;
import static com.tunjid.fingergestures.gestureconsumers.GestureConsumer.TOGGLE_FLASHLIGHT;


public class ActionViewHolder extends BaseViewHolder<ActionAdapter.ActionClickListener> {

    private final boolean showsText;
    private int action;
    private TextView textView;
    private ImageView imageView;

    public ActionViewHolder(boolean showsText, View itemView, ActionAdapter.ActionClickListener clickListener) {
        super(itemView, clickListener);
        this.showsText = showsText;
        textView = itemView.findViewById(R.id.text);
        imageView = itemView.findViewById(R.id.icon);

        itemView.setOnClickListener(view -> adapterListener.onActionClicked(action));
    }

    public void bind(@GestureConsumer.GestureAction int action) {
        this.action = action;

        textView.setVisibility(showsText ? View.VISIBLE : View.GONE);
        textView.setText(GestureMapper.getInstance().resourceForAction(action));

        int iconRes = actionToIcon(action);
        int iconColor = BrightnessGestureConsumer.getInstance().getSliderColor();

        if (showsText) imageView.setImageResource(iconRes);
        else imageView.setImageDrawable(BackgroundManager.getInstance().tint(iconRes, iconColor));
    }

    private int actionToIcon(@GestureConsumer.GestureAction int action) {
        switch (action) {
            default:
            case DO_NOTHING:
                return R.drawable.ic_blank_24dp;

            case INCREASE_BRIGHTNESS:
                return R.drawable.ic_brightness_medium_24dp;

            case REDUCE_BRIGHTNESS:
                return R.drawable.ic_brightness_4_24dp;

            case MAXIMIZE_BRIGHTNESS:
                return R.drawable.ic_brightness_7_24dp;

            case MINIMIZE_BRIGHTNESS:
                return R.drawable.ic_brightness_low_24dp;

            case NOTIFICATION_UP:
                return R.drawable.ic_boxed_arrow_up_24dp;

            case NOTIFICATION_DOWN:
                return R.drawable.ic_boxed_arrow_down_24dp;

            case TOGGLE_FLASHLIGHT:
                return R.drawable.ic_brightness_flash_light_24dp;

            case TOGGLE_DOCK:
                return R.drawable.ic_arrow_collapse_down_24dp;

            case TOGGLE_AUTO_ROTATE:
                return R.drawable.ic_auto_rotate_24dp;
        }
    }
}
