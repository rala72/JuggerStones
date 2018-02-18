package contador.piedras.jugger.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import contador.piedras.jugger.JuggerStonesApplication;
import contador.piedras.jugger.LocaleUtils;
import contador.piedras.jugger.R;
import contador.piedras.jugger.model.CounterTask;

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
    protected AppCompatTextView textView_team1_points;
    @BindView(R.id.textView_team2_points)
    protected AppCompatTextView textView_team2_points;
    @BindView(R.id.textView_stones)
    protected AppCompatTextView textView_stones;

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
        Bundle extras = intent.getExtras();
        final long stones = extras != null ? extras.getLong(MyPreferenceActivity.KEY_COUNTER, 0L) : 0;
        final String team1 = extras != null ? extras.getString(MyPreferenceActivity.KEY_TEAM1, getResources().getString(R.string.team1)) : getString(R.string.team1);
        final String team2 = extras != null ? extras.getString(MyPreferenceActivity.KEY_TEAM2, getResources().getString(R.string.team2)) : getString(R.string.team2);
        initStonesView(stones);
        textView_team1.setText(team1);
        textView_team2.setText(team2);
        final int res = JuggerStonesApplication.CounterPreference.isInfinityMode() ?
                R.drawable.ic_infinity : JuggerStonesApplication.CounterPreference.isReverse() ?
                R.drawable.ic_sort_descending : R.drawable.ic_sort_ascending_modified;
        imageView_info.setImageDrawable(AppCompatResources.getDrawable(this, res));
    }
    //endregion

    //region stonesView
    private void initStonesView(long l) {
        l = cleanStones(l);
        final long mode = JuggerStonesApplication.CounterPreference.getMode();
        final boolean reverse = JuggerStonesApplication.CounterPreference.isReverse();
        if (reverse) {
            if (l == 0) l = mode;
        } else if (l % mode == 0) l = 0;
        textView_stones.setText(String.valueOf(l));
    }

    private long cleanStones(long l) {
        return cleanStones(l, false);
    }

    private long cleanStones(long l, boolean fromInput) {
        if (JuggerStonesApplication.CounterPreference.isInfinityMode()) return l;
        final long mode = JuggerStonesApplication.CounterPreference.getMode();
        final boolean reverse = JuggerStonesApplication.CounterPreference.isReverse();
        return reverse && l % mode == 0 ? mode : // entered a number % mode -> show mode instead of 0
                reverse && !fromInput ?
                        mode * (1 + (l / mode)) - l : // regular mode: calc next number (don't go over mode)
                        l - mode * (l / mode); // input mode: decrease number so no redundant modes are available
    }
    //endregion

    //region butterKnife:listeners
    @OnClick({R.id.button_team1_increase, R.id.button_team2_increase, R.id.button_stones_increase})
    protected void onIncreaseClick(AppCompatImageButton button) {
        long number;
        switch (button.getId()) {
            case R.id.button_team1_increase:
                number = Long.parseLong(textView_team1_points.getText().toString());
                textView_team1_points.setText(String.valueOf(number + 1));
                checkIfStopAfterPoint();
                break;
            case R.id.button_team2_increase:
                number = Long.parseLong(textView_team2_points.getText().toString());
                textView_team2_points.setText(String.valueOf(number + 1));
                checkIfStopAfterPoint();
                break;
            case R.id.button_stones_increase:
                if (isTimerRunning()) return;
                number = Long.parseLong(textView_stones.getText().toString());
                textView_stones.setText(String.valueOf(number + 1));
                break;
        }
    }

    @OnClick({R.id.button_team1_decrease, R.id.button_team2_decrease, R.id.button_stones_decrease})
    protected void onDecreaseClick(AppCompatImageButton button) {
        long number;
        switch (button.getId()) {
            case R.id.button_team1_decrease:
                number = Long.parseLong(textView_team1_points.getText().toString());
                if (0 < number) textView_team1_points.setText(String.valueOf(number - 1));
                break;
            case R.id.button_team2_decrease:
                number = Long.parseLong(textView_team2_points.getText().toString());
                if (0 < number) textView_team2_points.setText(String.valueOf(number - 1));
                break;
            case R.id.button_stones_decrease:
                if (isTimerRunning()) return;
                number = Long.parseLong(textView_stones.getText().toString());
                if (0 < number) textView_stones.setText(String.valueOf(number - 1));
                break;
        }
    }

    @OnLongClick({R.id.button_team1_decrease, R.id.button_team2_decrease, R.id.button_stones_decrease})
    protected boolean onDecreaseLongClick(AppCompatImageButton button) {
        long number;
        switch (button.getId()) {
            case R.id.button_team1_decrease:
                number = Long.parseLong(textView_team1_points.getText().toString());
                if (0 < number) textView_team1_points.setText(String.valueOf(0));
                break;
            case R.id.button_team2_decrease:
                number = Long.parseLong(textView_team2_points.getText().toString());
                if (0 < number) textView_team2_points.setText(String.valueOf(0));
                break;
            case R.id.button_stones_decrease:
                if (isTimerRunning()) return true;
                number = Long.parseLong(textView_stones.getText().toString());
                if (0 < number) textView_stones.setText(String.valueOf(0));
                break;
        }
        return true;
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
    protected boolean onTeamNameLongClick() {
        renameTeams();
        return true;
    }

    @OnLongClick(R.id.textView_stones)
    protected boolean onCounterLongClick() {
        setStones();
        return true;
    }
    //endregion

    private void checkIfStopAfterPoint() {
        if (JuggerStonesApplication.sharedPreferences.getBoolean(JuggerStonesApplication.PREFS.STOP_AFTER_POINT.toString(), false))
            pauseTimer();
    }

    //region dialogs
    @SuppressWarnings("ConstantConditions")
    private void renameTeams() {
        final int margin_dp = 25;
        final int margin_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin_dp, getResources().getDisplayMetrics());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.renameTeams);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin_px, 0, margin_px, 0);

        final EditText editText_name1 = new EditText(MainActivity.this);
        editText_name1.setLayoutParams(layoutParams);
        editText_name1.setHint(R.string.renameTeams_1);
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            editText_name1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LIMIT_TEAM_NAME_CHARACTERS_TO)});
        editText_name1.setText(textView_team1.getText());
        editText_name1.requestFocus();

        final EditText editText_name2 = new EditText(MainActivity.this);
        editText_name2.setLayoutParams(layoutParams);
        editText_name2.setHint(R.string.renameTeams_2);
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
                textView_team1.setText(R.string.team1);
                textView_team2.setText(R.string.team2);
            }
        });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            Toast.makeText(MainActivity.this, getString(R.string.toast_teamLength, 5), Toast.LENGTH_SHORT).show();
    }

    private void changeTeamColors(final TEAM team) {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setDialogId(team.equals(TEAM.TEAM1) ? 1 : team.equals(TEAM.TEAM2) ? 2 : 0)
                .setDialogTitle(R.string.changeColor)
                .setColor(team.equals(TEAM.TEAM1) ? textView_team1.getCurrentTextColor() : textView_team2.getCurrentTextColor())
                .setShowAlphaSlider(false)
                .setAllowCustom(false)
                .setSelectedButtonText(android.R.string.ok)
                .show(this);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            textView_team1.setTextColor(color);
            textView_team1_points.setTextColor(color);
        } else if (dialogId == 2) {
            textView_team2.setTextColor(color);
            textView_team2_points.setTextColor(color);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void resetTeams() {
        textView_team1.setText(R.string.team1);
        textView_team1.setTextColor(getResources().getColor(R.color.default_team1));
        textView_team1_points.setText("0");
        textView_team1_points.setTextColor(getResources().getColor(R.color.default_team1));
        textView_team2.setText(R.string.team2);
        textView_team2.setTextColor(getResources().getColor(R.color.default_team2));
        textView_team2_points.setText("0");
        textView_team2_points.setTextColor(getResources().getColor(R.color.default_team2));
    }

    private void setStones() {
        if (isTimerRunning()) return;
        final int margin_dp = 25;
        final int margin_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin_dp, getResources().getDisplayMetrics());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.setStones);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin_px, 0, margin_px, 0);

        final EditText stonesEdit = new EditText(MainActivity.this);
        stonesEdit.setHint(R.string.setStones);
        stonesEdit.setLayoutParams(layoutParams);
        stonesEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        stonesEdit.setText(textView_stones.getText());
        stonesEdit.requestFocus();

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(stonesEdit);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                final long stones = !stonesEdit.getText().toString().isEmpty() ? Long.parseLong(stonesEdit.getText().toString()) : 0;
                textView_stones.setText(String.valueOf(cleanStones(stones, true)));
            }
        });
        alertDialogBuilder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initStonesView(0);
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        //noinspection ConstantConditions // why!?
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
    //endregion

    //region timer
    protected void startTimer() {
        button_playPause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_pause_circle));
        if (isTimerRunning()) return;
        long stones = Long.parseLong(textView_stones.getText().toString().trim());
        stones = cleanStones(stones);
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
        initStonesView(0);
    }

    protected boolean isTimerRunning() {
        return timer != null;
    }

    @Override
    public void onStonesChanged(final long stones) {
        final long mode = JuggerStonesApplication.CounterPreference.getMode();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_stones.setText(String.valueOf(cleanStones(stones)));
                if (JuggerStonesApplication.CounterPreference.isInfinityMode() && stones > 0 && stones % JuggerStonesApplication.DEFAULT_INTERVAL == 0)
                    Toast.makeText(MainActivity.this, R.string.toast_infinity, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onGongPlayed(final long stones) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean pauseAfterGong = JuggerStonesApplication.sharedPreferences
                        .getBoolean(JuggerStonesApplication.PREFS.STOP_AFTER_GONG.toString(), false);
                if (pauseAfterGong) pauseTimer();
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
            case R.id.teams_reset:
                resetTeams();
                return true;
            case R.id.editStones:
                setStones();
                return true;
            case R.id.action_support:
                intent = new Intent(this, SupportActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.action_settings:
                pauseTimer();
                intent = new Intent(this, MyPreferenceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = new Bundle();
                bundle.putLong(MyPreferenceActivity.KEY_COUNTER, Long.parseLong(textView_stones.getText().toString()));
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
                JuggerStonesApplication.increaseVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                JuggerStonesApplication.decreaseVolume();
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
