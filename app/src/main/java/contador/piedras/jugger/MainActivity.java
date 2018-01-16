package contador.piedras.jugger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import contador.piedras.jugger.model.Sound;
import contador.piedras.jugger.preference.AppPreferences;

public class MainActivity extends AppCompatActivity {

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

    private AudioManager audio;
    private boolean isPaused = true;
    private Counter counter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textView_stones.setText(String.valueOf(extras.getLong(AppPreferences.KEY_COUNTER, 0L)));
            textView_team1.setText(extras.getString(AppPreferences.KEY_TEAM1, getResources().getString(R.string.team1)));
            textView_team2.setText(extras.getString(AppPreferences.KEY_TEAM2, getResources().getString(R.string.team2)));
        } else {
            textView_stones.setText(String.valueOf(0));
            textView_team1.setText(getResources().getString(R.string.team1));
            textView_team2.setText(getResources().getString(R.string.team2));
        }
    }

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
                long mode = Long.parseLong(sharedPreferences.getString("mode", "100"));
                long interval = Long.parseLong(sharedPreferences.getString("interval", "1500"));
                String soundStone = sharedPreferences.getString("time_sounds", "stone");
                String soundGong = sharedPreferences.getString("gong_sounds", "vuvuzela");
                Sound sound = new Sound(getApplicationContext(), soundStone, soundGong);

                isPaused = !isPaused;
                button_play.setImageResource(isPaused ? R.drawable.play : R.drawable.pause);
                if (isPaused) counter.setStopped(true);
                else {
                    counter = new Counter(getApplicationContext(), textView_stones, stones, mode, interval, sound, button_play);
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

    private void checkIfStopAfterPoint() {
        if (sharedPreferences.getBoolean("stop_after_point", false)) {
            button_play.setImageResource(R.drawable.play);
            counter.setStopped(true);
        }
    }

    // dialogs
    private void setStones() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.setStones);
        final EditText stonesEdit = new EditText(MainActivity.this);
        stonesEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        stonesEdit.requestFocus();

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
                } catch (NumberFormatException e) {
                    textView_stones.setText(String.valueOf(0));
                }
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        alertDialogBuilder.create().show();
    }

    private void renameTeams() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder.setTitle(R.string.renameTeams);
        final EditText editText_name1 = new EditText(MainActivity.this);
        editText_name1.setHint(R.string.renameTeams_1);
        editText_name1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        final EditText editText_name2 = new EditText(MainActivity.this);
        editText_name2.setHint(R.string.renameTeams_2);
        editText_name2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText_name1);
        linearLayout.addView(editText_name2);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                textView_team1.setText(editText_name1.getText().toString());
                textView_team2.setText(editText_name2.getText().toString());
            }
        });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        alertDialogBuilder.create().show();
        Toast.makeText(MainActivity.this, R.string.toast_teamLength, Toast.LENGTH_SHORT).show();
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

                Intent i = new Intent(this, AppPreferences.class);
                i.putExtra(AppPreferences.KEY_COUNTER, textView_stones.getText().toString());
                i.putExtra(AppPreferences.KEY_TEAM1, textView_team1.getText().toString());
                i.putExtra(AppPreferences.KEY_TEAM2, textView_team2.getText().toString());
                i.putExtra(AppPreferences.KEY_COUNT, textView_stones.getText().toString());
                startActivity(i);
                finish();

                return true;
            case R.id.set_stones:
                setStones();
                return true;
            case R.id.action_support:
                startActivity(new Intent(this, SupportActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            default:
                return true;
        }
    }
}
