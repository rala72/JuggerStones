package contador.piedras.jugger;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import java.util.Locale;

public class AppPreferences extends PreferenceActivity {

    private AudioManager audio;
    Bundle extras;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        extras = getIntent().getExtras();

        final ListPreference prefListLanguage = (ListPreference) findPreference("pref_language");

        prefListLanguage
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        changeLanguage(newValue.toString());
                        return true;
                    }
                });
    }

    private void changeLanguage(String language) {
        Locale newLocale = new Locale(language.toLowerCase(Locale.US), language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = newLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, AppPreferences.class);
        refresh.putExtra("Counter", extras.getString("Counter"));
        refresh.putExtra("Team 1", extras.getString("Team 1"));
        refresh.putExtra("Team 2", extras.getString("Team 2"));
        refresh.putExtra("count", extras.getString("count"));
        startActivity(refresh);
        finish();
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
                Intent a = new Intent(AppPreferences.this, MainActivity.class);
                a.putExtra("Counter", extras.getString("Counter"));
                a.putExtra("Team 1", extras.getString("Team 1"));
                a.putExtra("Team 2", extras.getString("Team 2"));
                a.putExtra("count", extras.getString("count"));
                startActivity(a);
                finish();
                return true;
            default:
                return true; // should be false..?
        }
    }
}