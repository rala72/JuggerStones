package contador.piedras.jugger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import java.util.Locale;

import contador.piedras.jugger.model.SoundPreferenceList;

public class MyPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_COUNTER = "counter";
    public static final String KEY_TEAM1 = "team1";
    public static final String KEY_TEAM2 = "team2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setListener();
        updateSumTexts();
    }

    private void setListener() {
        JuggerStonesApplication.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //region section:other
        final ListPreference pref_language = (ListPreference) findPreference(JuggerStonesApplication.PREFS.LANGUAGE.toString());
        pref_language.setDefaultValue(Locale.getDefault().getLanguage());
        pref_language.setValue(getResources().getConfiguration().locale.getLanguage());
        pref_language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                changeLanguage(newValue.toString());
                return true;
            }
        });

        final Preference pref_contact = findPreference(JuggerStonesApplication.PREFS.CONTACT.toString());
        pref_contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                JuggerStonesApplication.sendEmail(getApplicationContext());
                return true;
            }
        });
        final Preference pref_playStore = findPreference(JuggerStonesApplication.PREFS.PLAY_STORE.toString());
        pref_playStore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return true;
            }
        });
        //endregion
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSumTexts(); // reduce to only required prefs
    }

    private void updateSumTexts() {
        ListPreference preference_mode = (ListPreference) findPreference(JuggerStonesApplication.PREFS.MODE.toString());
        preference_mode.setSummary(preference_mode.getEntry());
        ListPreference preference_interval = (ListPreference) findPreference(JuggerStonesApplication.PREFS.INTERVAL.toString());
        preference_interval.setSummary(preference_interval.getEntry());
        SoundPreferenceList preference_sound = (SoundPreferenceList) findPreference(JuggerStonesApplication.PREFS.SOUND_STONE.toString());
        preference_sound.setSummary(preference_sound.getEntry());
        SoundPreferenceList preference_gong = (SoundPreferenceList) findPreference(JuggerStonesApplication.PREFS.SOUND_GONG.toString());
        preference_gong.setSummary(preference_gong.getEntry());
        ListPreference pref_language = (ListPreference) findPreference(JuggerStonesApplication.PREFS.LANGUAGE.toString());
        pref_language.setSummary(pref_language.getEntry());
    }

    private void changeLanguage(String language) {
        Configuration configuration = getResources().getConfiguration();
        configuration.locale = new Locale(language.toLowerCase());
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        Intent intent = new Intent(getApplicationContext(), MyPreferenceActivity.class);
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
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
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            default:
                return true; // should be false..?
        }
    }
}