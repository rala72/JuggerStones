package io.rala.jugger;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.rala.jugger.fragment.MainFragment;
import io.rala.jugger.fragment.PreferenceFragment;
import io.rala.jugger.model.Team;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {
    public static final String KEY_COUNTER = "counter";
    public static final String KEY_TEAM1 = "team1";
    public static final String KEY_TEAM2 = "team2";

    public MainActivity() {
        LocaleUtils.updateConfig(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(MainActivity.KEY_COUNTER)) {
            final long stones = extras.getLong(MainActivity.KEY_COUNTER);
            final Team team1 = extras.getParcelable(MainActivity.KEY_TEAM1);
            final Team team2 = extras.getParcelable(MainActivity.KEY_TEAM2);
            goToPreferenceFragment(stones, team1, team2, false);
        } else goToMainFragment();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                JuggerStonesApp.increaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                JuggerStonesApp.decreaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_BACK:
                Fragment container = getSupportFragmentManager().findFragmentById(R.id.container);
                if (container instanceof OnBackPressedListener) {
                    ((OnBackPressedListener) container).onBackPressed();
                    return true;
                }
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        Fragment container = getSupportFragmentManager().findFragmentById(R.id.container);
        if (container instanceof ColorPickerDialogListener)
            ((ColorPickerDialogListener) container).onColorSelected(dialogId, color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        Fragment container = getSupportFragmentManager().findFragmentById(R.id.container);
        if (container instanceof ColorPickerDialogListener)
            ((ColorPickerDialogListener) container).onDialogDismissed(dialogId);
    }

    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
    }

    public void changeLanguage(String language) {
        LocaleUtils.setLocale(new Locale(language));
        LocaleUtils.updateConfig(getApplication(), getResources().getConfiguration());

        Fragment container = getSupportFragmentManager().findFragmentById(R.id.container);
        Intent intent = new Intent(this, MainActivity.class);
        if (container != null && container.getArguments() != null)
            intent.putExtras(container.getArguments());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // region goTo
    // direct pass bundle..?
    private void goToMainFragment() {
        goToFragment(new MainFragment(), null);
    }

    public void goToMainFragment(long stones, Team team1, Team team2) {
        goToFragment(MainFragment.newInstance(stones, team1, team2), true);
    }

    public void goToPreferenceFragment(long stones, Team team1, Team team2) {
        goToPreferenceFragment(stones, team1, team2, true);
    }

    public void goToPreferenceFragment(long stones, Team team1, Team team2, boolean animation) {
        goToFragment(PreferenceFragment.newInstance(stones, team1, team2),
            animation ? false : null
        );
    }

    private void goToFragment(Fragment fragment, Boolean animationRightToLeft) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.disallowAddToBackStack();
        if (animationRightToLeft != null)
            if (animationRightToLeft)
                fragmentTransaction.setCustomAnimations(
                    R.anim.left_to_right_in, R.anim.left_to_right_out
                );
            else
                fragmentTransaction.setCustomAnimations(
                    R.anim.right_to_left_in, R.anim.right_to_left_out
                );
        fragmentTransaction.replace(R.id.container, fragment).commit();
    }
    // endregion

    public interface OnBackPressedListener {
        void onBackPressed();
    }
}
