package io.rala.jugger.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.rala.jugger.JuggerStonesApplication;
import io.rala.jugger.LocaleUtils;
import io.rala.jugger.R;
import io.rala.jugger.model.CounterTask;
import io.rala.jugger.model.InputFilterMinMaxInteger;
import io.rala.jugger.view.NumberView;

public class MainActivity extends AppCompatActivity implements CounterTask.CounterTaskCallback, ColorPickerDialogListener {
    private static final int LIMIT_TEAM_NAME_CHARACTERS_TO = 0;

    //region butterKnife
    @BindView(R.id.button_playPause)
    protected AppCompatImageButton button_playPause;
    @BindView(R.id.textView_team1)
    protected AppCompatTextView textView_team1;
    @BindView(R.id.textView_team2)
    protected AppCompatTextView textView_team2;
    @BindView(R.id.textView_team1_points)
    protected NumberView textView_team1_points;
    @BindView(R.id.textView_team2_points)
    protected NumberView textView_team2_points;
    @BindView(R.id.textView_stones)
    protected NumberView textView_stones;

    @BindView(R.id.imageView_info)
    protected AppCompatImageView imageView_info;
    //endregion

    private Timer timer;

    private enum TEAM {TEAM1, TEAM2}

    public MainActivity() {
        LocaleUtils.updateConfig(this);
    }

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        applyBundle(getIntent());
    }

    private void applyBundle(Intent intent) {
        if (intent == null) intent = getIntent();
        final Bundle extras = intent.getExtras();
        final long stones = extras != null ? extras.getLong(MyPreferenceActivity.KEY_COUNTER, JuggerStonesApplication.CounterPreference.getModeStart()) : JuggerStonesApplication.CounterPreference.getModeStart();
        final String team1 = extras != null ? extras.getString(MyPreferenceActivity.KEY_TEAM1, getResources().getString(R.string.main_team1)) : getString(R.string.main_team1);
        final String team2 = extras != null ? extras.getString(MyPreferenceActivity.KEY_TEAM2, getResources().getString(R.string.main_team2)) : getString(R.string.main_team2);
        initStonesView(stones);
        textView_team1.setText(team1);
        textView_team2.setText(team2);
        updateInfoView();
    }

    private void updateInfoView() {
        final int resId = JuggerStonesApplication.CounterPreference.isInfinityMode() ?
                R.drawable.ic_infinity : JuggerStonesApplication.CounterPreference.isReverse() ?
                R.drawable.ic_sort_descending : R.drawable.ic_sort_ascending_modified;
        imageView_info.setImageDrawable(AppCompatResources.getDrawable(this, resId));
    }
    //endregion

    //region stonesView
    private void initStonesView(long l) {
        textView_stones.setNumber(cleanStones(l));
    }

    private static long cleanStones(long l) {
        if (l < JuggerStonesApplication.CounterPreference.getModeMin())
            return JuggerStonesApplication.CounterPreference.getModeStart();
        if (JuggerStonesApplication.CounterPreference.getModeMax() < l)
            return (l %= 2 * JuggerStonesApplication.CounterPreference.getMode()) < JuggerStonesApplication.CounterPreference.getModeMax() ?
                    l : l % JuggerStonesApplication.CounterPreference.getMode();
        if (JuggerStonesApplication.CounterPreference.isReverse() && l == 0)
            return JuggerStonesApplication.CounterPreference.getMode();
        return l;
    }

    /**
     * input mode
     *
     * @see #cleanStones(long) // , boolean
     */
    private void cleanStonesView() {
        textView_stones.setNumber(cleanStones(textView_stones.getNumberAsLong()));
    }
    //endregion

    //region butterKnife:listeners
    @OnClick({R.id.button_team1_increase, R.id.button_team2_increase, R.id.button_stones_increase})
    protected void onIncreaseClick(AppCompatImageButton button) {
        switch (button.getId()) {
            case R.id.button_team1_increase:
                textView_team1_points.increase(1L);
                if (JuggerStonesApplication.CounterPreference.isStopAfterPoint()) pauseTimer();
                break;
            case R.id.button_team2_increase:
                textView_team2_points.increase(1L);
                if (JuggerStonesApplication.CounterPreference.isStopAfterPoint()) pauseTimer();
                break;
            case R.id.button_stones_increase:
                if (isTimerRunning()) return;
                if (textView_stones.getNumberAsLong() < JuggerStonesApplication.CounterPreference.getModeMax()) textView_stones.increase(1L);
                break;
        }
    }

    @OnLongClick(R.id.button_stones_increase)
    protected boolean onIncreaseLongClick(AppCompatImageButton button) {
        if (button.getId() == R.id.button_stones_increase) {
            if (isTimerRunning()) return false;
            if (JuggerStonesApplication.CounterPreference.isReverse()) {
                textView_stones.setNumber(JuggerStonesApplication.CounterPreference.getMode());
                return true;
            } // other: increase by 10..?
        }
        return false;
    }

    @OnClick({R.id.button_team1_decrease, R.id.button_team2_decrease, R.id.button_stones_decrease})
    protected void onDecreaseClick(AppCompatImageButton button) {
        switch (button.getId()) {
            case R.id.button_team1_decrease:
                if (0 < textView_team1_points.getNumberAsLong()) textView_team1_points.decrease(1L);
                break;
            case R.id.button_team2_decrease:
                if (0 < textView_team2_points.getNumberAsLong()) textView_team2_points.decrease(1L);
                break;
            case R.id.button_stones_decrease:
                if (isTimerRunning()) return;
                if (JuggerStonesApplication.CounterPreference.getModeMin() < textView_stones.getNumberAsLong()) textView_stones.decrease(1L);
                break;
        }
    }

    @OnLongClick({R.id.button_team1_decrease, R.id.button_team2_decrease, R.id.button_stones_decrease})
    protected boolean onDecreaseLongClick(AppCompatImageButton button) {
        switch (button.getId()) {
            case R.id.button_team1_decrease:
                if (textView_team1_points.getNumberAsLong() == 0) return false;
                textView_team1_points.setNumber(0);
                return true;
            case R.id.button_team2_decrease:
                if (textView_team2_points.getNumberAsLong() == 0) return false;
                textView_team2_points.setNumber(0);
                return true;
            case R.id.button_stones_decrease:
                if (isTimerRunning()) return false;
                if (textView_stones.getNumberAsLong() == 0) return false;
                textView_stones.setNumber(0);
                return true;
            default:
                return false;
        }
    }

    @OnClick({R.id.button_playPause, R.id.button_stop})
    protected void onPlayPauseStopClick(AppCompatImageButton button) {
        switch (button.getId()) {
            case R.id.button_playPause:
                toggleTimer();
                break;
            case R.id.button_stop:
                stopTimer();
                break;
        }
    }

    @OnLongClick({R.id.textView_team1, R.id.textView_team2})
    protected boolean onTeamNameLongClick(TextView textView) {
        switch (textView.getId()) {
            case R.id.textView_team1:
                changeTeamColors(TEAM.TEAM1);
                return true;
            case R.id.textView_team2:
                changeTeamColors(TEAM.TEAM2);
                return true;
            default:
                return false;
        }
    }

    @OnLongClick(R.id.textView_stones)
    protected boolean onCounterLongClick(@SuppressWarnings("unused") TextView textView) {
        setStones();
        return true;
    }

    @OnClick(R.id.imageView_info)
    protected void onInfoViewClick(@SuppressWarnings("unused") AppCompatImageView imageView) {
        if (isTimerRunning()) return;
        SharedPreferences.Editor editor = JuggerStonesApplication.sharedPreferences.edit();
        editor.putString(JuggerStonesApplication.PREFS.MODE.toString(), String.valueOf(JuggerStonesApplication.CounterPreference.getPreviousMode()));
        editor.putString(JuggerStonesApplication.PREFS.MODE_PREVIOUS.toString(), String.valueOf(JuggerStonesApplication.CounterPreference.getMode()));
        editor.apply();
        updateInfoView();
        cleanStonesView();
    }

    @OnLongClick(R.id.imageView_info)
    protected boolean onInfoViewLongClick(@SuppressWarnings("unused") AppCompatImageView imageView) {
        if (isTimerRunning()) return false;
        if (JuggerStonesApplication.CounterPreference.isNormalModeIgnoringReverse()) {
            SharedPreferences.Editor editor = JuggerStonesApplication.sharedPreferences.edit();
            editor.putBoolean(JuggerStonesApplication.PREFS.REVERSE.toString(), !JuggerStonesApplication.CounterPreference.isReverse());
            editor.apply();
            updateInfoView();
            cleanStonesView();
            return true;
        }
        return false;
    }
    //endregion

    //region dialogs
    @SuppressWarnings("ConstantConditions")
    private void renameTeams() {
        final int margin_dp = 25;
        final int margin_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin_dp, getResources().getDisplayMetrics());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.main_renameTeams);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin_px, 0, margin_px, 0);

        final EditText editText_name1 = new EditText(MainActivity.this);
        editText_name1.setLayoutParams(layoutParams);
        editText_name1.setHint(R.string.main_renameTeams_1);
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            editText_name1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LIMIT_TEAM_NAME_CHARACTERS_TO)});
        editText_name1.setText(textView_team1.getText());
        editText_name1.requestFocus();

        final EditText editText_name2 = new EditText(MainActivity.this);
        editText_name2.setLayoutParams(layoutParams);
        editText_name2.setHint(R.string.main_renameTeams_2);
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            editText_name2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LIMIT_TEAM_NAME_CHARACTERS_TO)});
        editText_name2.setText(textView_team2.getText());

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText_name1);
        linearLayout.addView(editText_name2);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String name1 = editText_name1.getText().toString().trim();
                String name2 = editText_name2.getText().toString().trim();
                if (!name1.isEmpty()) textView_team1.setText(name1);
                if (!name2.isEmpty()) textView_team2.setText(name2);
            }
        });
        alertDialogBuilder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView_team1.setText(R.string.main_team1);
                textView_team2.setText(R.string.main_team2);
            }
        });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            Toast.makeText(MainActivity.this, getString(R.string.main_toast_teamLength, 5), Toast.LENGTH_SHORT).show();
    }

    private void changeTeamColors(final TEAM team) {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setDialogId(team.equals(TEAM.TEAM1) ? 1 : team.equals(TEAM.TEAM2) ? 2 : 0)
                .setDialogTitle(R.string.main_changeColor)
                .setColor(team.equals(TEAM.TEAM1) ? textView_team1.getCurrentTextColor() : textView_team2.getCurrentTextColor())
                .setShowAlphaSlider(false)
                .setAllowCustom(false)
                .setSelectedButtonText(android.R.string.ok)
                .show(this);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                textView_team1.setTextColor(color);
                textView_team1_points.setTextColor(color);
                break;
            case 2:
                textView_team2.setTextColor(color);
                textView_team2_points.setTextColor(color);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // do nothing
    }

    private void flipTeams() {
        CharSequence team1_title = textView_team1.getText();
        ColorStateList team1_title_color = textView_team1.getTextColors();
        long team1_points = textView_team1_points.getNumberAsLong();
        ColorStateList team1_points_color = textView_team1_points.getTextColors();

        textView_team1.setText(textView_team2.getText());
        textView_team1.setTextColor(textView_team2.getTextColors());
        textView_team1_points.setNumber(textView_team2_points.getNumberAsLong());
        textView_team1_points.setTextColor(textView_team2_points.getTextColors());

        textView_team2.setText(team1_title);
        textView_team2.setTextColor(team1_title_color);
        textView_team2_points.setNumber(team1_points);
        textView_team2_points.setTextColor(team1_points_color);
    }

    private void resetTeams() {
        textView_team1.setText(R.string.main_team1);
        textView_team1.setTextColor(getResources().getColor(R.color.default_team1));
        textView_team1_points.setNumber(0L);
        textView_team1_points.setTextColor(getResources().getColor(R.color.default_team1));
        textView_team2.setText(R.string.main_team2);
        textView_team2.setTextColor(getResources().getColor(R.color.default_team2));
        textView_team2_points.setNumber(0L);
        textView_team2_points.setTextColor(getResources().getColor(R.color.default_team2));
    }

    private void setStones() {
        if (isTimerRunning()) return;
        final int margin_dp = 25;
        final int margin_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin_dp, getResources().getDisplayMetrics());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.main_setStones);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin_px, 0, margin_px, 0);

        final EditText stonesEdit = new EditText(MainActivity.this);
        stonesEdit.setHint(R.string.main_setStones);
        stonesEdit.setLayoutParams(layoutParams);
        stonesEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        stonesEdit.setFilters(new InputFilter[]{new InputFilterMinMaxInteger(JuggerStonesApplication.CounterPreference.getModeMin(), JuggerStonesApplication.CounterPreference.getModeMax())});
        stonesEdit.setText(textView_stones.getText());
        stonesEdit.requestFocus();

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(stonesEdit);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                final String input = stonesEdit.getText().toString();
                final long stones = !input.isEmpty() && !input.equals("-") ?
                        Long.parseLong(stonesEdit.getText().toString()) : JuggerStonesApplication.CounterPreference.getModeStart();
                textView_stones.setNumber(cleanStones(stones));
            }
        });
        alertDialogBuilder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initStonesView(JuggerStonesApplication.CounterPreference.getModeStart());
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        //noinspection ConstantConditions // why!?
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
    //endregion

    //region timer & CounterTaskCallback
    protected void startTimer() {
        button_playPause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_pause_circle));
        if (isTimerRunning()) return;
        final long stones = cleanStones(textView_stones.getNumberAsLong());
        final long mode = JuggerStonesApplication.CounterPreference.getMode();
        final long interval = JuggerStonesApplication.CounterPreference.getInterval();
        final long delay = JuggerStonesApplication.CounterPreference.isImmediateStart() ? 0 : interval;
        final CounterTask counterTask = new CounterTask(this, stones, mode, JuggerStonesApplication.sound, this);
        timer = new Timer();
        timer.scheduleAtFixedRate(counterTask, delay, interval);
    }

    protected void pauseTimer() {
        button_playPause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_play_circle));
        if (!isTimerRunning()) return;
        timer.cancel();
        timer = null;
    }

    protected void toggleTimer() {
        if (!isTimerRunning()) startTimer();
        else pauseTimer();
    }

    protected void stopTimer() {
        pauseTimer();
        initStonesView(JuggerStonesApplication.CounterPreference.getModeStart());
    }

    protected boolean isTimerRunning() {
        return timer != null;
    }

    @Override
    public void onStonesChanged(final long stones) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_stones.setNumber(cleanStones(stones));
                if (JuggerStonesApplication.CounterPreference.isInfinityMode() && stones > 0 && stones % JuggerStonesApplication.DEFAULT_INTERVAL == 0)
                    Toast.makeText(MainActivity.this, R.string.main_toast_infinity, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onGongPlayed(final long stones) {
        if (JuggerStonesApplication.CounterPreference.isStopAfterGong())
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pauseTimer();
                }
            });
    }
    //endregion

    //region @Override
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.teams_rename:
                renameTeams();
                return true;
            case R.id.teams_changeColor_1:
                changeTeamColors(TEAM.TEAM1);
                return true;
            case R.id.teams_changeColor_2:
                changeTeamColors(TEAM.TEAM2);
                return true;
            case R.id.teams_changeOfEnds:
                flipTeams();
                return true;
            case R.id.teams_reset:
                resetTeams();
                return true;
            case R.id.editStones:
                setStones();
                return true;
            case R.id.action_settings:
                pauseTimer();
                intent = new Intent(this, MyPreferenceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = new Bundle();
                bundle.putLong(MyPreferenceActivity.KEY_COUNTER, textView_stones.getNumberAsLong());
                bundle.putString(MyPreferenceActivity.KEY_TEAM1, textView_team1.getText().toString());
                bundle.putString(MyPreferenceActivity.KEY_TEAM2, textView_team2.getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                JuggerStonesApplication.increaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                JuggerStonesApplication.decreaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            default:
                return true;
        }
    }
    //endregion
}
