package contador.piedras.jugger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity {

	Button play, stop;
	Button plust1, plust2;
	Button min1, min2;

	TextView T1Score, T2Score;
	TextView T1Name, T2Name;
	TextView Counter;
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

		Counter = (TextView) findViewById(R.id.TV_cero);

		play = (Button) findViewById(R.id.b_start);
		stop = (Button) findViewById(R.id.b_stop);

		plust1 = (Button) findViewById(R.id.b_mast1);
		plust2 = (Button) findViewById(R.id.b_mast2);
		min1 = (Button) findViewById(R.id.b_mint1);
		min2 = (Button) findViewById(R.id.b_mint2);

		T1Score = (TextView) findViewById(R.id.TV_pointT1);
		T2Score = (TextView) findViewById(R.id.TV_pointT2);

		T1Name = (TextView) findViewById(R.id.TV_nameT1);
		T2Name = (TextView) findViewById(R.id.TV_nameT2);

		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				int mode = Integer.parseInt(SP.getString("mode", "100"));
				int interval = Integer.parseInt(SP
						.getString("interval", "1500"));
				int soundStone = Integer.parseInt(SP.getString("time_sounds",
						R.raw.stone + ""));
				int soundGong = Integer.parseInt(SP.getString("gong_sounds",
						R.raw.vuvucela + ""));
				Sounds s = new Sounds(getApplicationContext(), soundStone,
						soundGong);

				if (isPaused) {// Pausar el contador
					play.setBackgroundResource(R.drawable.pause);
					isPaused = false;
					counter = new Counter(Counter, Integer.parseInt(Counter
							.getText().toString()), mode, interval, s);

					counter.start();
				} else {// Reanudar el contador	
					play.setBackgroundResource(R.drawable.play);
					isPaused = true;
					counter.setStoped(true);
				}
			}
		});
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isPaused) {
					play.setBackgroundResource(R.drawable.play);
					counter.setStoped(true);
					isPaused = true;
					Counter.setText("0");
				}
				
			}
		});
		plust1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int num = Integer.parseInt(T1Score.getText().toString());
				T1Score.setText((num + 1) + "");
			}
		});
		min1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (0 < Integer.parseInt(T1Score.getText().toString())) {
					int num = Integer.parseInt(T1Score.getText().toString());
					T1Score.setText((num - 1) + "");
				}
			}
		});
		plust2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int num = Integer.parseInt(T2Score.getText().toString());
				T2Score.setText((num + 1) + "");
			}
		});
		min2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (0 < Integer.parseInt(T2Score.getText().toString())) {
					int num = Integer.parseInt(T2Score.getText().toString());
					T2Score.setText((num - 1) + "");
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Listener of the context menu items
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.rename_teams:
			RenameTeams();
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, AppPreferences.class));
			return true;
		case R.id.Assit:
			startActivity(new Intent(this, Assit.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void RenameTeams() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				MainActivity.this);

		alertDialog.setTitle(R.string.rename_teams);
		final EditText NameT1 = new EditText(MainActivity.this);
		NameT1.setHint(R.string.team1ini);
		NameT1.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });

		final EditText NameT2 = new EditText(MainActivity.this);
		NameT2.setHint(R.string.team2ini);
		NameT2.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(NameT1);
		ll.addView(NameT2);
		alertDialog.setView(ll);

		alertDialog.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
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

}