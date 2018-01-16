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
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import contador.piedras.jugger.model.Counter;
import contador.piedras.jugger.model.Sound;
import contador.piedras.jugger.preference.AppPreferences;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private Button play, stop;
    private AppCompatImageButton min1, min2;
    private AppCompatImageButton incStones, decStones;

    private TextView T1Score, T2Score;
    private TextView T1Name, T2Name;
    private TextView tv_counter;
    private AudioManager audio;

    boolean isPaused = true;

    private Counter counter;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        tv_counter = findViewById(R.id.textView_stones);

        play = findViewById(R.id.button_play);
        stop = findViewById(R.id.button_stop);

        AppCompatImageButton plust1 = findViewById(R.id.button_team1_increase);
        AppCompatImageButton plust2 = findViewById(R.id.button_team2_increase);
        min1 = findViewById(R.id.button_team1_decrease);
        min2 = findViewById(R.id.button_team2_decrease);

        incStones = findViewById(R.id.button_stones_increase);
        decStones = findViewById(R.id.button_stones_decrease);

        T1Score = findViewById(R.id.textView_team1_points);
        T2Score = findViewById(R.id.textView_team2_points);

        T1Name = findViewById(R.id.textView_team1);
        T2Name = findViewById(R.id.textView_team2);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        plust1.setOnClickListener(this);
        min1.setOnClickListener(this);
        plust2.setOnClickListener(this);
        min2.setOnClickListener(this);
        tv_counter.setOnLongClickListener(this);
        T1Name.setOnLongClickListener(this);
        T2Name.setOnLongClickListener(this);
        incStones.setOnClickListener(this);
        decStones.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tv_counter.setText(extras.getString(AppPreferences.KEY_COUNTER, "0"));
            T1Name.setText(extras.getString(AppPreferences.KEY_TEAM1, getResources().getString(R.string.team1)));
            T2Name.setText(extras.getString(AppPreferences.KEY_TEAM2, getResources().getString(R.string.team2)));
        } else {
            tv_counter.setText("0");
            T1Name.setText(getResources().getString(R.string.team1));
            T2Name.setText(getResources().getString(R.string.team2));
        }
    }

    public void onClick(View v) {
        int num;
        switch (v.getId()) {
            case R.id.button_play:
                int mode = Integer.parseInt(sharedPreferences.getString("mode", "100"));
                int interval = Integer.parseInt(sharedPreferences.getString("interval", "1500"));
                String soundStone = sharedPreferences.getString("time_sounds", "stone");
                String soundGong = sharedPreferences.getString("gong_sounds", "vuvuzela");
                Sound s = new Sound(getApplicationContext(), soundStone, soundGong);

                if (isPaused) {// Pausar el contador
                    play.setBackgroundResource(R.drawable.pause);
                    isPaused = false;
                    counter = new Counter(tv_counter, Integer.parseInt(tv_counter.getText().toString()), mode, interval, s, getApplicationContext(), play);
                    counter.start();
                } else {// Reanudar el contador
                    play.setBackgroundResource(R.drawable.play);
                    isPaused = true;
                    counter.setStopped(true);
                }
                break;
            case R.id.button_stop:
                if (!isPaused) {
                    play.setBackgroundResource(R.drawable.play);
                    counter.setStopped(true);
                    isPaused = true;
                    tv_counter.setText("0");
                } else {
                    tv_counter.setText("0");
                }
                break;
            case R.id.button_team1_increase:
                num = Integer.parseInt(T1Score.getText().toString());
                T1Score.setText(String.valueOf(num + 1));
                if (sharedPreferences.getBoolean("stop_after_point", false)) {
                    play.setBackgroundResource(R.drawable.play);
                    counter.setStopped(true);
                }
                break;

            case R.id.button_team2_increase:
                num = Integer.parseInt(T2Score.getText().toString());
                T2Score.setText(String.valueOf(num + 1));
                if (sharedPreferences.getBoolean("stop_after_point", false)) {
                    play.setBackgroundResource(R.drawable.play);
                    counter.setStopped(true);
                }
                break;

            case R.id.button_team1_decrease:
                if (0 < Integer.parseInt(T1Score.getText().toString())) {
                    num = Integer.parseInt(T1Score.getText().toString());
                    T1Score.setText(String.valueOf(num - 1));
                }
                break;

            case R.id.button_team2_decrease:
                if (0 < Integer.parseInt(T2Score.getText().toString())) {
                    num = Integer.parseInt(T2Score.getText().toString());
                    T2Score.setText(String.valueOf(num - 1));
                }
                break;

            case R.id.button_stones_increase:
                num = Integer.parseInt(tv_counter.getText().toString());
                tv_counter.setText(String.valueOf(num + 1));
                break;

            case R.id.button_stones_decrease:
                if (0 < Integer.parseInt(tv_counter.getText().toString())) {
                    num = Integer.parseInt(tv_counter.getText().toString());
                    tv_counter.setText(String.valueOf(num - 1));
                }
                break;
        }
    }

    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.textView_team1:
                renameTeams();
                break;
            case R.id.textView_team2:
                renameTeams();
                break;
            case R.id.textView_stones:
                setCounter();
                break;
        }
        return true;
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
                play.setBackgroundResource(R.drawable.play);
                isPaused = true;
                if (counter != null) counter.setStopped(true);

                Intent i = new Intent(this, AppPreferences.class);
                i.putExtra(AppPreferences.KEY_COUNTER, tv_counter.getText().toString());
                i.putExtra(AppPreferences.KEY_TEAM1, T1Name.getText().toString());
                i.putExtra(AppPreferences.KEY_TEAM2, T2Name.getText().toString());
                i.putExtra(AppPreferences.KEY_COUNT, tv_counter.getText().toString());
                startActivity(i);
                finish();

                return true;
            case R.id.set_counter:
                setCounter();
                return true;
            case R.id.action_support:
                startActivity(new Intent(this, Support.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setCounter() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(R.string.set_counter);
        final EditText counterEdit = new EditText(MainActivity.this);
        counterEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        counterEdit.requestFocus();

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(counterEdit);
        alertDialog.setView(ll);

        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int num = Integer.parseInt(counterEdit.getText().toString());
                    tv_counter.setText(String.valueOf(num + ""));
                } catch (Exception e) {
                    tv_counter.setText(String.valueOf(0));
                }
            }
        });

        alertDialog.setNegativeButton(android.R.string.cancel, null);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void renameTeams() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle(R.string.renameTeams);
        final EditText NameT1 = new EditText(MainActivity.this);
        NameT1.setHint(R.string.renameTeams_1);
        NameT1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        final EditText NameT2 = new EditText(MainActivity.this);
        NameT2.setHint(R.string.renameTeams_2);
        NameT2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(NameT1);
        ll.addView(NameT2);
        alertDialog.setView(ll);

        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (NameT1.getText().toString().trim().length() > 5
                        || NameT1.getText().toString().trim().length() == 0
                        || NameT2.getText().toString().trim().length() > 5
                        || NameT2.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this,
                            R.string.toast_teamLength,
                            Toast.LENGTH_LONG).show();
                } else {
                    T1Name.setText(NameT1.getText().toString());
                    T2Name.setText(NameT2.getText().toString());
                }
            }
        });

        alertDialog.setNegativeButton(android.R.string.cancel, null);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // Para controlar el volumen
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;

            default:
                return true;
        }
    }
}
