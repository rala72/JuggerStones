package contador.piedras.jugger.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import contador.piedras.jugger.JuggerStonesApplication;
import contador.piedras.jugger.R;
import contador.piedras.jugger.model.CounterTask;

public class MainActivity extends AppCompatActivity implements CounterTask.CounterTaskCallback {
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
    //endregion

    private Timer timer;

    private enum TEAM {TEAM1, TEAM2}

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
        textView_stones.setText(String.valueOf(stones));
        textView_team1.setText(team1);
        textView_team2.setText(team2);
    }

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
    public void onDecreaseClick(AppCompatImageButton button) {
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
            stopTimer();
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
        final Map<String, Integer> colors = new TreeMap<>();
        colors.put(getString(R.string.color_green), getResources().getColor(R.color.green));
        colors.put(getString(R.string.color_red), getResources().getColor(R.color.red));
        colors.put(getString(R.string.color_yellow), getResources().getColor(R.color.yellow));

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(R.string.changeColor);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.array_colors));
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int color = colors.get(arrayAdapter.getItem(which));
                if (team.equals(TEAM.TEAM1)) {
                    textView_team1.setTextColor(color);
                    textView_team1_points.setTextColor(color);
                } else if (team.equals(TEAM.TEAM2)) {
                    textView_team2.setTextColor(color);
                    textView_team2_points.setTextColor(color);
                }
                dialog.dismiss();
            }
        });
        builderSingle.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.create().show();
    }

    private void resetTeams() {
        textView_team1.setText(R.string.team1);
        textView_team1.setTextColor(getResources().getColor(R.color.red));
        textView_team1_points.setText("0");
        textView_team1_points.setTextColor(getResources().getColor(R.color.red));
        textView_team2.setText(R.string.team2);
        textView_team2.setTextColor(getResources().getColor(R.color.green));
        textView_team2_points.setText("0");
        textView_team2_points.setTextColor(getResources().getColor(R.color.green));
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
                textView_stones.setText(String.valueOf(stones));
            }
        });
        alertDialogBuilder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView_stones.setText("0");
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
        button_playPause.setImageResource(R.drawable.button_pause);
        if (isTimerRunning()) return;
        final long stones = Long.parseLong(textView_stones.getText().toString().trim());
        final long mode = Long.parseLong(JuggerStonesApplication.sharedPreferences.getString(JuggerStonesApplication.PREFS.MODE.toString(), String.valueOf(JuggerStonesApplication.DEFAULT_MODE)));
        final long interval = Long.parseLong(JuggerStonesApplication.sharedPreferences.getString(JuggerStonesApplication.PREFS.INTERVAL.toString(), String.valueOf(JuggerStonesApplication.DEFAULT_INTERVAL)));
        final CounterTask counterTask = new CounterTask(this, stones, mode, JuggerStonesApplication.sound, this);
        timer = new Timer();
        timer.scheduleAtFixedRate(counterTask, 0, interval);
    }

    protected void pauseTimer() {
        button_playPause.setImageResource(R.drawable.button_play);
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
        onStonesChanged(0);
    }

    protected boolean isTimerRunning() {
        return timer != null;
    }

    @Override
    public void onStonesChanged(final long stones) {
        final long mode = Long.parseLong(JuggerStonesApplication.sharedPreferences.getString(JuggerStonesApplication.PREFS.MODE.toString(), String.valueOf(JuggerStonesApplication.DEFAULT_MODE)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_stones.setText(String.valueOf(stones));
                if (mode == -1 && stones > 0 && stones % 100 == 0)
                    Toast.makeText(getApplicationContext(), R.string.toast_infinity, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onGongPlayed(final long stones) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean pauseAfterGong = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getBoolean(JuggerStonesApplication.PREFS.STOP_AFTER_GONG.toString(), false);
                if (pauseAfterGong) pauseTimer();
            }
        });
    }
    //endregion

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
}
