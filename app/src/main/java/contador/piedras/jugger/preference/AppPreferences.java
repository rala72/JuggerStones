package contador.piedras.jugger.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import java.util.Locale;

import contador.piedras.jugger.MainActivity;
import contador.piedras.jugger.R;

public class AppPreferences extends PreferenceActivity {
    public static final String KEY_COUNTER = "Counter";
    public static final String KEY_TEAM1 = "Team 1";
    public static final String KEY_TEAM2 = "Team 2";
    public static final String KEY_COUNT = "count";

    private AudioManager audio;
    private Bundle extras;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        extras = getIntent().getExtras();

        final ListPreference prefListLanguage = (ListPreference) findPreference("pref_language");
        prefListLanguage.setDefaultValue(Locale.getDefault().getLanguage());
        prefListLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                changeLanguage(newValue.toString());
                return true;
            }
        });
    }

    private void changeLanguage(String language) {
        Configuration configuration = getResources().getConfiguration();
        configuration.locale = new Locale(language.toLowerCase(Locale.ENGLISH), language);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        Intent intent = new Intent(this, AppPreferences.class);
        intent.putExtra(KEY_COUNTER, extras.getString(KEY_COUNTER));
        intent.putExtra(KEY_TEAM1, extras.getString(KEY_TEAM1));
        intent.putExtra(KEY_TEAM2, extras.getString(KEY_TEAM2));
        intent.putExtra(KEY_COUNT, extras.getString(KEY_COUNT));
        startActivity(intent);
        finish();
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
                Intent a = new Intent(AppPreferences.this, MainActivity.class);
                a.putExtra(KEY_COUNTER, extras.getString(KEY_COUNTER));
                a.putExtra(KEY_TEAM1, extras.getString(KEY_TEAM1));
                a.putExtra(KEY_TEAM2, extras.getString(KEY_TEAM2));
                a.putExtra(KEY_COUNT, extras.getString(KEY_COUNT));
                startActivity(a);
                finish();
                return true;
            default:
                return true; // should be false..?
        }
    }
}