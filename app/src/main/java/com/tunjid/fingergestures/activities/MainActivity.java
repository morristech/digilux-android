package com.tunjid.fingergestures.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextSwitcher;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.animator.ViewHider;
import com.tunjid.fingergestures.App;
import com.tunjid.fingergestures.BackgroundManager;
import com.tunjid.fingergestures.R;
import com.tunjid.fingergestures.TrialView;
import com.tunjid.fingergestures.baseclasses.FingerGestureActivity;
import com.tunjid.fingergestures.billing.PurchasesManager;
import com.tunjid.fingergestures.fragments.AppFragment;
import com.tunjid.fingergestures.models.State;
import com.tunjid.fingergestures.models.TextLink;
import com.tunjid.fingergestures.viewholders.DiffViewHolder;
import com.tunjid.fingergestures.viewmodels.AppViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.CompositeDisposable;

import static android.content.Intent.ACTION_SEND;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.animation.AnimationUtils.loadAnimation;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.LENGTH_SHORT;
import static com.tunjid.fingergestures.BackgroundManager.ACTION_EDIT_WALLPAPER;
import static com.tunjid.fingergestures.services.FingerGestureService.ACTION_SHOW_SNACK_BAR;
import static com.tunjid.fingergestures.services.FingerGestureService.EXTRA_SHOW_SNACK_BAR;

public class MainActivity extends FingerGestureActivity {

    public static final int STORAGE_CODE = 100;
    public static final int SETTINGS_CODE = 200;
    public static final int ACCESSIBILITY_CODE = 300;
    public static final int DO_NOT_DISTURB_CODE = 400;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STORAGE_CODE, SETTINGS_CODE, ACCESSIBILITY_CODE, DO_NOT_DISTURB_CODE})
    public @interface PermissionRequest {}

    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};

    private ViewGroup constraintLayout;
    private TextSwitcher switcher;
    private MaterialButton permissionText;
    private BottomSheetBehavior bottomSheetBehavior;

    private FabExtensionAnimator fabExtensionAnimator;
    private CompositeDisposable disposables;

    private AppViewModel viewModel;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_EDIT_WALLPAPER.equals(action))
                showSnackbar(R.string.error_wallpaper_google_photos);
            else if (ACTION_SHOW_SNACK_BAR.equals(action))
                showSnackbar(intent.getIntExtra(EXTRA_SHOW_SNACK_BAR, R.string.generic_error));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        disposables = new CompositeDisposable();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));

        Toolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        switcher = findViewById(R.id.upgrade_prompt);
        permissionText = findViewById(R.id.permission_view);
        constraintLayout = findViewById(R.id.constraint_layout);

        fabHider = ViewHider.of(permissionText).setDirection(ViewHider.BOTTOM).build();
        barHider = ViewHider.of(toolbar).setDirection(ViewHider.TOP).build();
        fabExtensionAnimator = new FabExtensionAnimator(permissionText);

        fabHider.hide();

        permissionText.setBackgroundTintList(getFabTint());
        permissionText.setOnClickListener(view -> viewModel.onPermissionClicked(this::onPermissionClicked));
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);

        disposables.add(viewModel.state().subscribe(this::onStateChanged, Throwable::printStackTrace));

        setSupportActionBar(toolbar);
        toggleBottomSheet(false);

        Intent startIntent = getIntent();
        boolean isPickIntent = startIntent != null && ACTION_SEND.equals(startIntent.getAction());

        if (savedInstanceState == null && isPickIntent) handleIntent(startIntent);
        else if (savedInstanceState == null)
            showAppFragment(viewModel.gestureItems);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm,
                                              @NonNull Fragment f,
                                              @NonNull View v,
                                              @Nullable Bundle savedInstanceState) {
                if (f instanceof AppFragment)
                    updateBottomNav((AppFragment) f, bottomNavigationView);
            }
        }, false);

        setUpSwitcher();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PurchasesManager.getInstance().hasAds()) shill();
        else hideAds();

        if (!App.accessibilityServiceEnabled()) requestPermission(ACCESSIBILITY_CODE);
        invalidateOptionsMenu();

        IntentFilter filter = new IntentFilter(ACTION_EDIT_WALLPAPER);
        filter.addAction(ACTION_SHOW_SNACK_BAR);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_start_trial);
        boolean isTrialVisible = !PurchasesManager.getInstance().isPremiumNotTrial();

        if (item != null) item.setVisible(isTrialVisible);
        if (isTrialVisible && item != null) item.setActionView(new TrialView(this, item));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_trial:
                PurchasesManager purchasesManager = PurchasesManager.getInstance();
                boolean isTrialRunning = purchasesManager.isTrialRunning();

                Snackbar snackbar = Snackbar.make(coordinator, purchasesManager.getTrialPeriodText(), isTrialRunning ? LENGTH_SHORT : LENGTH_INDEFINITE);
                if (!isTrialRunning) snackbar.setAction(android.R.string.yes, view -> {
                    purchasesManager.startTrial();
                    recreate();
                });

                snackbar.show();
                break;
            case R.id.action_directions:
                showAppFragment(viewModel.gestureItems);
                return true;
            case R.id.action_slider:
                showAppFragment(viewModel.brightnessItems);
                return true;
            case R.id.action_audio:
                showAppFragment(viewModel.audioItems);
                return true;
            case R.id.action_accessibility_popup:
                showAppFragment(viewModel.popupItems);
                return true;
            case R.id.action_wallpaper:
                showAppFragment(viewModel.appearanceItems);
                return true;
            case R.id.info:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.open_source_libraries)
                        .setItems(viewModel.links, (dialog, index) -> showLink(viewModel.links[index]))
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != STATE_HIDDEN) toggleBottomSheet(false);
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        DiffViewHolder.onActivityDestroyed();

        if (disposables != null) disposables.clear();
        switcher = null;
        constraintLayout = null;
        permissionText = null;
        bottomSheetBehavior = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.onPermissionChange(requestCode).ifPresent(this::showSnackbar);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != STORAGE_CODE || grantResults.length == 0 || grantResults[0] != PERMISSION_GRANTED)
            return;

        viewModel.onPermissionChange(requestCode).ifPresent(this::showSnackbar);
        AppFragment fragment = (AppFragment) getCurrentFragment();
        if (fragment != null) fragment.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean showFragment(BaseFragment fragment) {
        viewModel.checkPermissions();
        return super.showFragment(fragment);
    }

    public void requestPermission(@PermissionRequest int permission) {
        viewModel.requestPermission(permission);
    }

    public void toggleBottomSheet(boolean show) {
        bottomSheetBehavior.setState(show ? STATE_COLLAPSED : STATE_HIDDEN);
    }

    private void showAppFragment(int[] items) {
        showFragment(AppFragment.newInstance(items));
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (!ACTION_SEND.equals(action) || type == null || !type.startsWith("image/")) return;

        if (!App.hasStoragePermission()) {
            showSnackbar(R.string.enable_storage_settings);
            showAppFragment(viewModel.gestureItems);
            return;
        }

        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri == null) return;

        AppFragment toShow = AppFragment.newInstance(viewModel.appearanceItems);
        final String tag = toShow.getStableTag();

        showFragment(toShow);

        BackgroundManager.getInstance().requestWallPaperConstant(R.string.choose_target, this, selection -> {
            AppFragment shown = (AppFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (shown != null && shown.isVisible()) shown.cropImage(imageUri, selection);
            else showSnackbar(R.string.error_wallpaper);
        });
    }

    private void askForStorage() {
        showPermissionDialog(R.string.wallpaper_permission_request, () -> requestPermissions(STORAGE_PERMISSIONS, STORAGE_CODE));
    }

    private void askForSettings() {
        showPermissionDialog(R.string.settings_permission_request, () -> startActivityForResult(App.settingsIntent(), SETTINGS_CODE));
    }

    private void askForAccessibility() {
        showPermissionDialog(R.string.accessibility_permissions_request, () -> startActivityForResult(App.accessibilityIntent(), ACCESSIBILITY_CODE));
    }

    private void askForDoNotDisturb() {
        showPermissionDialog(R.string.do_not_disturb_permissions_request, () -> startActivityForResult(App.doNotDisturbIntent(), DO_NOT_DISTURB_CODE));
    }

    private void showPermissionDialog(@StringRes int stringRes, Runnable yesAction) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(stringRes)
                .setPositiveButton(R.string.yes, (dialog, b) -> yesAction.run())
                .setNegativeButton(R.string.no, (dialog, b) -> dialog.dismiss())
                .show();
    }

    private void onPermissionClicked(int permissionRequest) {
        if (permissionRequest == DO_NOT_DISTURB_CODE) askForDoNotDisturb();
        else if (permissionRequest == ACCESSIBILITY_CODE) askForAccessibility();
        else if (permissionRequest == SETTINGS_CODE) askForSettings();
        else if (permissionRequest == STORAGE_CODE) askForStorage();
    }

    private void updateBottomNav(@NonNull AppFragment fragment, BottomNavigationView bottomNavigationView) {
        viewModel.updateBottomNav(Arrays.hashCode(fragment.getItems())).ifPresent(bottomNavigationView::setSelectedItemId);
    }

    private void showLink(TextLink textLink) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(textLink.link));
        startActivity(browserIntent);
    }

    private void shill() {
        disposables.add(viewModel.shill().subscribe(switcher::setText, Throwable::printStackTrace));
    }

    private void hideAds() {
        viewModel.calmIt();
        if (switcher.getVisibility() == View.GONE) return;

        Transition hideTransition = getTransition();
        hideTransition.addListener(new TransitionListenerAdapter() {
            public void onTransitionEnd(Transition transition) { showSnackbar(R.string.billing_thanks); }
        });
        TransitionManager.beginDelayedTransition(constraintLayout, hideTransition);
        switcher.setVisibility(View.GONE);
    }

    private void setUpSwitcher() {
        switcher.setFactory(() -> {
            View view = LayoutInflater.from(this).inflate(R.layout.text_switch, switcher, false);
            view.setOnClickListener(clicked -> viewModel.shillMoar());
            return view;
        });

        switcher.setInAnimation(loadAnimation(this, android.R.anim.slide_in_left));
        switcher.setOutAnimation(loadAnimation(this, android.R.anim.slide_out_right));
    }

    private void onStateChanged(State state) {
        fabExtensionAnimator.updateGlyphs(state.glyphState);
        permissionText.post(state.fabVisible ? fabHider::show : fabHider::hide);
    }

    private ColorStateList getFabTint() {
        return ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private Transition getTransition() {
        return new AutoTransition().excludeTarget(RecyclerView.class, true);
    }
}
