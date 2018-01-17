package contador.piedras.jugger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import contador.piedras.jugger.model.Counter;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_APP_PREFERENCES = 0;
    private static final int LIMIT_TEAM_NAME_CHARACTERS_TO = 0;

    //region butterKnife
    @BindView(R.id.button_playPause)
    protected AppCompatImageButton button_play;
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

    private boolean isPaused = true;
    private Counter counter;

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
        if (extras != null) {
            textView_stones.setText(String.valueOf(extras.getLong(MyPreferenceActivity.KEY_COUNTER, 0L)));
            textView_team1.setText(extras.getString(MyPreferenceActivity.KEY_TEAM1, getResources().getString(R.string.team1)));
            textView_team2.setText(extras.getString(MyPreferenceActivity.KEY_TEAM2, getResources().getString(R.string.team2)));
        } else {
            textView_stones.setText(String.valueOf(0));
            textView_team1.setText(R.string.team1);
            textView_team2.setText(R.string.team2);
        }
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
                number = Long.parseLong(textView_stones.getText().toString());
                if (0 < number) textView_stones.setText(String.valueOf(number - 1));
                break;
        }
    }

    @OnClick({R.id.button_playPause, R.id.button_stop})
    protected void onPlayPauseStopClick(AppCompatImageButton button) {
        switch (button.getId()) {
            case R.id.button_playPause:
                long stones = Long.parseLong(textView_stones.getText().toString().trim());
                long mode = JuggerStonesApplication.sharedPreferences.getLong(JuggerStonesApplication.PREFS.MODE.toString(), 100);
                long interval = JuggerStonesApplication.sharedPreferences.getLong(JuggerStonesApplication.PREFS.INTERVAL.toString(), 1500);

                isPaused = !isPaused;
                button_play.setImageResource(isPaused ? R.drawable.play : R.drawable.pause);
                if (isPaused) counter.setStopped(true);
                else {
                    counter = new Counter(getApplicationContext(), textView_stones, stones, mode, interval, JuggerStonesApplication.sound, button_play);
                    counter.start();
                }
                break;
            case R.id.button_stop:
                if (!isPaused) {
                    isPaused = true;
                    button_play.setImageResource(R.drawable.play);
                    counter.setStopped(true);
                }
                textView_stones.setText("0");
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
        if (JuggerStonesApplication.sharedPreferences.getBoolean(JuggerStonesApplication.PREFS.STOP_AFTER_POINT.toString(), false)) {
            button_play.setImageResource(R.drawable.play);
            counter.setStopped(true);
        }
    }

    // dialogs
    private void setStones() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.setStones);
        final EditText stonesEdit = new EditText(MainActivity.this);
        stonesEdit.setHint(R.string.setStones);
        stonesEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        stonesEdit.setText(textView_stones.getText());

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(stonesEdit);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    long number = Long.parseLong(stonesEdit.getText().toString());
                    textView_stones.setText(String.valueOf(number));
                } catch (NumberFormatException e) { // shouldn't be possible
                    textView_stones.setText(String.valueOf(0));
                }
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        alertDialogBuilder.create().show();
    }

    @SuppressWarnings("ConstantConditions")
    private void renameTeams() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder.setTitle(R.string.renameTeams);
        final EditText editText_name1 = new EditText(MainActivity.this);
        editText_name1.setHint(R.string.renameTeams_1);
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            editText_name1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LIMIT_TEAM_NAME_CHARACTERS_TO)});
        editText_name1.setText(textView_team1.getText());

        final EditText editText_name2 = new EditText(MainActivity.this);
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
                if (name1.isEmpty() && name2.isEmpty()) {
                    textView_team1.setText(R.string.team1);
                    textView_team2.setText(R.string.team2);
                    textView_team1_points.setText("0");
                    textView_team2_points.setText("0");
                }
            }
        });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        alertDialogBuilder.create().show();
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            Toast.makeText(MainActivity.this, getString(R.string.toast_teamLength, 5), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rename_teams:
                renameTeams();
                return true;
            case R.id.action_settings:
                button_play.setImageResource(R.drawable.play);
                isPaused = true;
                if (counter != null) counter.setStopped(true);
                Intent intent = new Intent(this, MyPreferenceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong(MyPreferenceActivity.KEY_COUNTER, Long.parseLong(textView_stones.getText().toString()));
                bundle.putString(MyPreferenceActivity.KEY_TEAM1, textView_team1.getText().toString());
                bundle.putString(MyPreferenceActivity.KEY_TEAM2, textView_team2.getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
                return true;
            case R.id.set_stones:
                setStones();
                return true;
            case R.id.action_support:
                startActivity(new Intent(this, SupportActivity.class));
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
