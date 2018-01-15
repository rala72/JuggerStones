package contador.piedras.jugger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    Button play, stop;
    Button plust1, plust2;
    Button min1, min2;
    Button incStones, decStones;

    TextView T1Score, T2Score;
    TextView T1Name, T2Name;
    TextView tv_counter;
    private AudioManager audio;

    boolean isPaused = true;

    Counter counter;

    SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        tv_counter = findViewById(R.id.TV_cero);

        play = findViewById(R.id.b_start);
        stop = findViewById(R.id.b_stop);

        plust1 = findViewById(R.id.b_mast1);
        plust2 = findViewById(R.id.b_mast2);
        min1 = findViewById(R.id.b_mint1);
        min2 = findViewById(R.id.b_mint2);

        incStones = findViewById(R.id.inc_stones);
        decStones = findViewById(R.id.dec_stones);

        T1Score = findViewById(R.id.TV_pointT1);
        T2Score = findViewById(R.id.TV_pointT2);

        T1Name = findViewById(R.id.TV_nameT1);
        T2Name = findViewById(R.id.TV_nameT2);

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

        enlargeAreas();

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

    public void enlargeAreas() { //TODO no funciona, comprobar
        final View parent = (View) decStones.getParent();  // button: the view you want to enlarge hit area
        parent.post(new Runnable() {
            public void run() {
                Rect rect = new Rect();
                decStones.getHitRect(rect);
                rect.top -= 10000;    // increase top hit area
                rect.left -= 10000;   // increase left hit area
                rect.bottom += 10000; // increase bottom hit area
                rect.right += 10000;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect, decStones));
            }
        });

        final View auxminT1 = (View) min1.getParent();  // button: the view you want to enlarge hit area
        auxminT1.post(new Runnable() {
            public void run() {
                Rect rect = new Rect();
                min1.getHitRect(rect);
                rect.top -= 10;    // increase top hit area
                rect.bottom += 10; // increase bottom hit area
                auxminT1.setTouchDelegate(new TouchDelegate(rect, min1));
            }
        });

        final View auxminT2 = (View) min2.getParent();  // button: the view you want to enlarge hit area
        auxminT2.post(new Runnable() {
            public void run() {
                Rect rect = new Rect();
                min2.getHitRect(rect);
                rect.top -= 10;    // increase top hit area
                rect.bottom += 10; // increase bottom hit area
                auxminT2.setTouchDelegate(new TouchDelegate(rect, min2));
            }
        });
    }

    public void onClick(View v) {
        int num;
        switch (v.getId()) {
            case R.id.b_start:
                int mode = Integer.parseInt(SP.getString("mode", "100"));
                int interval = Integer.parseInt(SP.getString("interval", "1500"));
                int soundStone = Integer.parseInt(SP.getString("time_sounds", R.raw.stone + ""));
                int soundGong = Integer.parseInt(SP.getString("gong_sounds", R.raw.vuvucela + ""));
                Sounds s = new Sounds(getApplicationContext(), soundStone, soundGong);

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
            case R.id.b_stop:
                if (!isPaused) {
                    play.setBackgroundResource(R.drawable.play);
                    counter.setStopped(true);
                    isPaused = true;
                    tv_counter.setText("0");
                } else {
                    tv_counter.setText("0");
                }
                break;
            case R.id.b_mast1:
                num = Integer.parseInt(T1Score.getText().toString());
                T1Score.setText(String.valueOf(num + 1));
                if (SP.getBoolean("stop_after_point", false)) {
                    play.setBackgroundResource(R.drawable.play);
                    counter.setStopped(true);
                }
                break;

            case R.id.b_mast2:
                num = Integer.parseInt(T2Score.getText().toString());
                T2Score.setText(String.valueOf(num + 1));
                if (SP.getBoolean("stop_after_point", false)) {
                    play.setBackgroundResource(R.drawable.play);
                    counter.setStopped(true);
                }
                break;

            case R.id.b_mint1:
                if (0 < Integer.parseInt(T1Score.getText().toString())) {
                    num = Integer.parseInt(T1Score.getText().toString());
                    T1Score.setText(String.valueOf(num - 1));
                }
                break;

            case R.id.b_mint2:
                if (0 < Integer.parseInt(T2Score.getText().toString())) {
                    num = Integer.parseInt(T2Score.getText().toString());
                    T2Score.setText(String.valueOf(num - 1));
                }
                break;

            case R.id.inc_stones:
                num = Integer.parseInt(tv_counter.getText().toString());
                tv_counter.setText(String.valueOf(num + 1));
                break;

            case R.id.dec_stones:
                if (0 < Integer.parseInt(tv_counter.getText().toString())) {
                    num = Integer.parseInt(tv_counter.getText().toString());
                    tv_counter.setText(String.valueOf(num - 1));
                }
                break;
        }
    }

    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.TV_nameT1:
                renameTeams();
                break;
            case R.id.TV_nameT2:
                renameTeams();
                break;
            case R.id.TV_cero:
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
            case R.id.Assit:
                startActivity(new Intent(this, Assit.class));
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

        alertDialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
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

        alertDialog.setNegativeButton(R.string.cancel, null);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void renameTeams() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle(R.string.rename_teams);
        final EditText NameT1 = new EditText(MainActivity.this);
        NameT1.setHint(R.string.team1ini);
        NameT1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        final EditText NameT2 = new EditText(MainActivity.this);
        NameT2.setHint(R.string.team2ini);
        NameT2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(NameT1);
        ll.addView(NameT2);
        alertDialog.setView(ll);

        alertDialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (NameT1.getText().toString().trim().length() > 5
                        || NameT1.getText().toString().trim().length() == 0
                        || NameT2.getText().toString().trim().length() > 5
                        || NameT2.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this,
                            R.string.warning_initials,
                            Toast.LENGTH_LONG).show();
                } else {
                    T1Name.setText(NameT1.getText().toString());
                    T2Name.setText(NameT2.getText().toString());
                }
            }
        });

        alertDialog.setNegativeButton(R.string.cancel, null);

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

    protected void onStart() {
        super.onStart();
        //tv_counter.setText("0");
    }
}
