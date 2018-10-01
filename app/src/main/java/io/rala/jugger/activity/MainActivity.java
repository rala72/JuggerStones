package io.rala.jugger.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import io.rala.jugger.model.HistoryEntry;
import io.rala.jugger.model.InputFilterMinMaxInteger;
import io.rala.jugger.model.Team;
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

    private final TimerHandler timerHandler = new TimerHandler();
    private final ValueHandler valueHandler = new ValueHandler();

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
        if (JuggerStonesApplication.CounterPreference.isKeepDisplayAwake()) {
            // Toast.makeText(this, R.string.pref_keep_display_awake, Toast.LENGTH_SHORT).show();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void applyBundle(Intent intent) {
        if (intent == null) intent = getIntent();
        final Bundle extras = intent.getExtras();
        final long stones = extras != null ? extras.getLong(MyPreferenceFragment.KEY_COUNTER, JuggerStonesApplication.CounterPreference.getModeStart()) : JuggerStonesApplication.CounterPreference.getModeStart();
        final Team team1 = extras != null ? (Team) extras.getParcelable(MyPreferenceFragment.KEY_TEAM1) : null;
        final Team team2 = extras != null ? (Team) extras.getParcelable(MyPreferenceFragment.KEY_TEAM2) : null;
        valueHandler.setStones(stones);
        valueHandler.setTeams(team1, team2);
        updateInfoView();
    }

    private void updateInfoView() {
        final int resId = JuggerStonesApplication.CounterPreference.isInfinityMode() ?
                R.drawable.ic_infinity : JuggerStonesApplication.CounterPreference.isReverse() ?
                R.drawable.ic_sort_descending : R.drawable.ic_sort_ascending_modified;
        imageView_info.setImageDrawable(AppCompatResources.getDrawable(this, resId));
    }
    //endregion

    //region butterKnife:listeners
    @OnClick({R.id.button_team1_increase, R.id.button_team2_increase, R.id.button_stones_increase})
    protected void onIncreaseClick(AppCompatImageButton button) {
        switch (button.getId()) {
            case R.id.button_team1_increase:
                textView_team1_points.increase(1L);
                if (JuggerStonesApplication.CounterPreference.isStopAfterPoint()) timerHandler.pause();
                break;
            case R.id.button_team2_increase:
                textView_team2_points.increase(1L);
                if (JuggerStonesApplication.CounterPreference.isStopAfterPoint()) timerHandler.pause();
                break;
            case R.id.button_stones_increase:
                if (timerHandler.isRunning()) return;
                if (textView_stones.getNumberAsLong() < JuggerStonesApplication.CounterPreference.getModeMax()) textView_stones.increase(1L);
                break;
        }
    }

    @OnLongClick(R.id.button_stones_increase)
    protected boolean onIncreaseLongClick(AppCompatImageButton button) {
        if (button.getId() == R.id.button_stones_increase) {
            if (timerHandler.isRunning()) return false;
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
                if (timerHandler.isRunning()) return;
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
                if (timerHandler.isRunning()) return false;
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
                timerHandler.toggle();
                break;
            case R.id.button_stop:
                timerHandler.stop();
                break;
        }
    }

    @OnLongClick({R.id.textView_team1, R.id.textView_team2})
    protected boolean onTeamNameLongClick(TextView textView) {
        switch (textView.getId()) {
            case R.id.textView_team1:
                showChangeTeamColorsDialog(TEAM.TEAM1);
                return true;
            case R.id.textView_team2:
                showChangeTeamColorsDialog(TEAM.TEAM2);
                return true;
            default:
                return false;
        }
    }

    @OnLongClick(R.id.textView_stones)
    protected boolean onCounterLongClick(@SuppressWarnings("unused") TextView textView) {
        showSetStonesDialog();
        return true;
    }

    @OnClick(R.id.imageView_info)
    protected void onInfoViewClick(@SuppressWarnings("unused") AppCompatImageView imageView) {
        if (timerHandler.isRunning()) return;
        valueHandler.toggleNormalModeWithInfinity();
    }

    @OnLongClick(R.id.imageView_info)
    protected boolean onInfoViewLongClick(@SuppressWarnings("unused") AppCompatImageView imageView) {
        if (timerHandler.isRunning()) return false;
        if (JuggerStonesApplication.CounterPreference.isNormalModeIgnoringReverse()) {
            valueHandler.toggleNormalModeWithReverse();
            return true;
        }
        return false;
    }
    //endregion

    //region dialogs
    @SuppressWarnings("ConstantConditions")
    private void showRenameTeamsDialog() {
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

    private void showChangeTeamColorsDialog(TEAM team) {
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
                valueHandler.setTeamColor(ColorStateList.valueOf(color), TEAM.TEAM1);
                break;
            case 2:
                valueHandler.setTeamColor(ColorStateList.valueOf(color), TEAM.TEAM2);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // do nothing
    }

    private void showSetStonesDialog() {
        if (timerHandler.isRunning()) return;
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
                valueHandler.setStones(stones);
            }
        });
        alertDialogBuilder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueHandler.resetStones();
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        //noinspection ConstantConditions // why!?
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
    //endregion

    //region CounterTaskCallback
    @Override
    public void onStonesChanged(final long stones) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                valueHandler.setStones(stones);
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
                    timerHandler.pause();
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
                showRenameTeamsDialog();
                return true;
            case R.id.teams_changeColor_1:
                showChangeTeamColorsDialog(TEAM.TEAM1);
                return true;
            case R.id.teams_changeColor_2:
                showChangeTeamColorsDialog(TEAM.TEAM2);
                return true;
            case R.id.teams_changeOfEnds:
                valueHandler.flipTeams();
                return true;
            case R.id.teams_reset:
                valueHandler.resetTeams();
                return true;
            case R.id.editStones:
                showSetStonesDialog();
                return true;
            case R.id.history_lastScore:
                valueHandler.applyHistoryEntry(JuggerStonesApplication.getLastHistoryEntry());
                return true;
            case R.id.action_settings:
                timerHandler.pause();
                intent = new Intent(this, MyPreferenceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = new Bundle();
                bundle.putLong(MyPreferenceFragment.KEY_COUNTER, textView_stones.getNumberAsLong());
                bundle.putParcelable(MyPreferenceFragment.KEY_TEAM1, valueHandler.getTeam1());
                bundle.putParcelable(MyPreferenceFragment.KEY_TEAM2, valueHandler.getTeam2());
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

    private class TimerHandler {
        private Timer timer;

        void start() {
            button_playPause.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_pause_circle));
            if (isRunning()) return;
            final long stones = valueHandler.getStones();
            final long mode = JuggerStonesApplication.CounterPreference.getMode();
            final long interval = JuggerStonesApplication.CounterPreference.getInterval();
            final long delay = JuggerStonesApplication.CounterPreference.isImmediateStart() ? 0 : interval;
            valueHandler.saveHistoryEntry(stones, mode);
            final CounterTask counterTask = new CounterTask(MainActivity.this, stones, mode, JuggerStonesApplication.sound, MainActivity.this);
            timer = new Timer();
            timer.scheduleAtFixedRate(counterTask, delay, interval);
        }

        void pause() {
            button_playPause.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_play_circle));
            if (!isRunning()) return;
            timer.cancel();
            timer = null;
        }

        void toggle() {
            if (!isRunning()) start();
            else pause();
        }

        void stop() {
            pause();
            valueHandler.resetStones();
            valueHandler.clearHistory();
        }

        boolean isRunning() {
            return timer != null;
        }
    }

    private class ValueHandler {
        void reset() {
            resetTeams();
            resetStones();
            // reset mode..?
        }

        //region teams
        void setTeam(Team team, TEAM teamNumber) {
            if (teamNumber == null || teamNumber == TEAM.TEAM1) {
                if (team.getName() != null) textView_team1.setText(team.getName());
                if (team.getNameColor() != null) textView_team1.setTextColor(team.getNameColor());
                if (team.getPoints() != null) textView_team1_points.setNumber(team.getPoints());
                if (team.getPointsColor() != null) textView_team1_points.setTextColor(team.getPointsColor());
            }
            if (teamNumber == null || teamNumber == TEAM.TEAM2) {
                if (team.getName() != null) textView_team2.setText(team.getName());
                if (team.getNameColor() != null) textView_team2.setTextColor(team.getNameColor());
                if (team.getPoints() != null) textView_team2_points.setNumber(team.getPoints());
                if (team.getPointsColor() != null) textView_team2_points.setTextColor(team.getPointsColor());
            }
        }

        void setTeams(Team team1, Team team2) {
            if (team1 == null && team2 == null) resetTeams();
            else {
                setTeam(team1, TEAM.TEAM1);
                setTeam(team2, TEAM.TEAM2);
            }
        }

        void setTeamColor(ColorStateList color, TEAM team) {
            if (team == null || team == TEAM.TEAM1) {
                if (color != null) textView_team1.setTextColor(color);
                if (color != null) textView_team1_points.setTextColor(color);
            }
            if (team == null || team == TEAM.TEAM2) {
                if (color != null) textView_team2.setTextColor(color);
                if (color != null) textView_team2_points.setTextColor(color);
            }
        }

        void flipTeams() {
            setTeams(getTeam2(), getTeam1());
        }

        void resetTeams() {
            final ColorStateList colorTeam1 = ColorStateList.valueOf(getResources().getColor(R.color.default_team1));
            final ColorStateList colorTeam2 = ColorStateList.valueOf(getResources().getColor(R.color.default_team2));
            setTeams(new Team(getString(R.string.main_team1), colorTeam1, 0L, colorTeam1),
                    new Team(getString(R.string.main_team2), colorTeam2, 0L, colorTeam2));
        }

        Team getTeam1() {
            CharSequence name = textView_team1.getText();
            ColorStateList name_color = textView_team1.getTextColors();
            long points = textView_team1_points.getNumberAsLong();
            ColorStateList points_color = textView_team1_points.getTextColors();
            return new Team(name, name_color, points, points_color);
        }

        Team getTeam2() {
            CharSequence name = textView_team2.getText();
            ColorStateList name_color = textView_team2.getTextColors();
            long points = textView_team2_points.getNumberAsLong();
            ColorStateList points_color = textView_team2_points.getTextColors();
            return new Team(name, name_color, points, points_color);
        }
        //endregion

        //region stones
        void setStones(long l) {
            textView_stones.setNumber(cleanStones(l));
        }

        public void resetStones() {
            setStones(JuggerStonesApplication.CounterPreference.getModeStart());
        }

        long getStones() {
            return cleanStones(textView_stones.getNumberAsLong());
        }

        /**
         * input mode
         *
         * @see #cleanStones(long)
         */
        void cleanStonesView() {
            textView_stones.setNumber(cleanStones(textView_stones.getNumberAsLong()));
        }

        private long cleanStones(long l) {
            if (l < JuggerStonesApplication.CounterPreference.getModeMin())
                return JuggerStonesApplication.CounterPreference.getModeStart();
            if (JuggerStonesApplication.CounterPreference.getModeMax() < l)
                return (l %= 2 * JuggerStonesApplication.CounterPreference.getMode()) < JuggerStonesApplication.CounterPreference.getModeMax() ?
                        l : l % JuggerStonesApplication.CounterPreference.getMode();
            if (JuggerStonesApplication.CounterPreference.isReverse() && l == 0)
                return JuggerStonesApplication.CounterPreference.getMode();
            return l;
        }
        //endregion

        //region mode
        void toggleNormalModeWithInfinity() {
            SharedPreferences.Editor editor = JuggerStonesApplication.sharedPreferences.edit();
            editor.putString(JuggerStonesApplication.PREFS.MODE.toString(), String.valueOf(JuggerStonesApplication.CounterPreference.getPreviousMode()));
            editor.putString(JuggerStonesApplication.PREFS.MODE_PREVIOUS.toString(), String.valueOf(JuggerStonesApplication.CounterPreference.getMode()));
            applyModeEditor(editor);
        }

        void toggleNormalModeWithReverse() {
            SharedPreferences.Editor editor = JuggerStonesApplication.sharedPreferences.edit();
            editor.putBoolean(JuggerStonesApplication.PREFS.REVERSE.toString(), !JuggerStonesApplication.CounterPreference.isReverse());
            applyModeEditor(editor);
        }

        private void applyModeEditor(SharedPreferences.Editor editor) {
            editor.apply();
            updateInfoView();
            valueHandler.cleanStonesView();
        }
        //endregion

        //region history
        private void applyHistoryEntry(HistoryEntry entry) {
            if (entry == null) {
                // Toast.makeText(MainActivity.this, R.string.reset, Toast.LENGTH_SHORT).show();
                reset();
                return;
            }
            setTeams(entry.getTeam1(), entry.getTeam2());
            setStones(entry.getStones());
            if (entry.getMode() != JuggerStonesApplication.CounterPreference.getMode())
                toggleNormalModeWithInfinity();
            if (JuggerStonesApplication.CounterPreference.isNormalModeIgnoringReverse() &&
                    entry.isReverse() != JuggerStonesApplication.CounterPreference.isReverse())
                toggleNormalModeWithReverse();
        }

        private void saveHistoryEntry(long stones, long mode) {
            JuggerStonesApplication.saveToHistory(new HistoryEntry(getTeam1(), getTeam2(), stones, mode, JuggerStonesApplication.CounterPreference.isReverse()));
        }

        private void clearHistory() {
            JuggerStonesApplication.clearHistory();
        }
        //endregion
    }
}
