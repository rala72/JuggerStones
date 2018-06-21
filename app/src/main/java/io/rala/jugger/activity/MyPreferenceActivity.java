package io.rala.jugger.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputFilter;
import android.view.KeyEvent;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import io.rala.jugger.JuggerStonesApplication;
import io.rala.jugger.LocaleUtils;
import io.rala.jugger.R;
import io.rala.jugger.model.InputFilterMinMaxDecimal;
import io.rala.jugger.model.InputFilterMinMaxInteger;
import io.rala.jugger.model.SoundPreferenceList;

public class MyPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_COUNTER = "counter";
    public static final String KEY_TEAM1 = "team1";
    public static final String KEY_TEAM2 = "team2";

    public MyPreferenceActivity() {
        LocaleUtils.updateConfig(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setFilter();
        setListener();
        updatePreferencesEnabled(null);
        updateLanguageText(); // has to be before sumTexts!
        updateSumTexts(null);
    }

    private void setFilter() {
        EditTextPreference mode_custom = (EditTextPreference) findPreference(JuggerStonesApplication.PREFS.MODE_CUSTOM.toString());
        mode_custom.getEditText().setFilters(new InputFilter[]{new InputFilterMinMaxInteger(BigInteger.valueOf(0))});
        EditTextPreference interval_custom = (EditTextPreference) findPreference(JuggerStonesApplication.PREFS.INTERVAL_CUSTOM.toString());
        interval_custom.getEditText().setFilters(new InputFilter[]{new InputFilterMinMaxDecimal(BigDecimal.valueOf(0))});
    }

    private void setListener() {
        JuggerStonesApplication.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //region section:other
        final ListPreference pref_language = (ListPreference) findPreference(JuggerStonesApplication.PREFS.LANGUAGE.toString());
        pref_language.setDefaultValue(LocaleUtils.DEFAULT_LOCALE.getLanguage());
        pref_language.setValue(LocaleUtils.getLocale().getLanguage());
        pref_language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                changeLanguage(newValue.toString());
                return true;
            }
        });

        final Preference pref_email = findPreference(JuggerStonesApplication.PREFS.EMAIL.toString());
        pref_email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                JuggerStonesApplication.sendEmail(MyPreferenceActivity.this);
                return true;
            }
        });
        final Preference pref_version = findPreference(JuggerStonesApplication.PREFS.VERSION.toString());
        pref_version.setTitle(getString(R.string.pref_version, JuggerStonesApplication.getVersion(this)));
        pref_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        validatePreferences(key);
        updatePreferencesEnabled(key);
        updateSumTexts(key);
    }

    //region updatePrefs
    private void validatePreferences(final String key) {
        if (key.equals(JuggerStonesApplication.PREFS.MODE_CUSTOM.toString())) {
            long min = 1;
            EditTextPreference mode_custom = (EditTextPreference) findPreference(JuggerStonesApplication.PREFS.MODE_CUSTOM.toString());
            long value = mode_custom.getText().trim().isEmpty() ? min : Long.parseLong(mode_custom.getText().trim());
            if (value == 0 || value == min) mode_custom.setText(String.valueOf(value));
        }
        if (key.equals(JuggerStonesApplication.PREFS.INTERVAL_CUSTOM.toString())) {
            double min = 0.001;
            EditTextPreference interval_custom = (EditTextPreference) findPreference(JuggerStonesApplication.PREFS.INTERVAL_CUSTOM.toString());
            double value = interval_custom.getText().trim().isEmpty() ? min : Double.parseDouble(interval_custom.getText().trim());
            if (value == 0 || value == min) interval_custom.setText(String.valueOf(value));
        }
    }

    private void updatePreferencesEnabled(final String key) {
        if (key == null || key.equals(JuggerStonesApplication.PREFS.MODE.toString())) {
            ListPreference mode = (ListPreference) findPreference(JuggerStonesApplication.PREFS.MODE.toString());
            findPreference(JuggerStonesApplication.PREFS.MODE_CUSTOM.toString()).setEnabled(mode.getValue().equals("0"));
            findPreference(JuggerStonesApplication.PREFS.REVERSE.toString()).setEnabled(!mode.getValue().equals("-1"));
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.INTERVAL.toString())) {
            ListPreference interval = (ListPreference) findPreference(JuggerStonesApplication.PREFS.INTERVAL.toString());
            findPreference(JuggerStonesApplication.PREFS.INTERVAL_CUSTOM.toString()).setEnabled(interval.getValue().equals("0"));
        }
    }

    private void updateLanguageText() {
        if (LocaleUtils.getLocale().equals(Locale.ENGLISH)) return;
        final String format = "%s (%s)";
        final ListPreference pref_language = (ListPreference) findPreference(JuggerStonesApplication.PREFS.LANGUAGE.toString());
        pref_language.setTitle(String.format(format, getString(R.string.language), getString(R.string.language_en)));
        // region languageMap
        Map<String, String> languageMap = new TreeMap<>();
        languageMap.put(getString(R.string.language_english), getString(R.string.language_english_en));
        languageMap.put(getString(R.string.language_catalan), getString(R.string.language_catalan_en));
        languageMap.put(getString(R.string.language_german), getString(R.string.language_german_en));
        languageMap.put(getString(R.string.language_spanish), getString(R.string.language_spanish_en));
        languageMap.put(getString(R.string.language_french), getString(R.string.language_french_en));
        languageMap.put(getString(R.string.language_portuguese), getString(R.string.language_portuguese_en));
        // endregion
        final String[] languages = getResources().getStringArray(R.array.array_languages_array);
        for (int i = 0; i < languages.length; i++)
            languages[i] = String.format(format, languages[i], languageMap.get(languages[i]));
        pref_language.setEntries(languages);
    }

    private void updateSumTexts(final String key) {
        if (key == null || key.equals(JuggerStonesApplication.PREFS.MODE.toString())) {
            ListPreference mode = (ListPreference) findPreference(JuggerStonesApplication.PREFS.MODE.toString());
            mode.setSummary(mode.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.MODE_CUSTOM.toString())) {
            EditTextPreference mode_custom = (EditTextPreference) findPreference(JuggerStonesApplication.PREFS.MODE_CUSTOM.toString());
            mode_custom.setSummary(mode_custom.getText());
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.INTERVAL.toString())) {
            ListPreference interval = (ListPreference) findPreference(JuggerStonesApplication.PREFS.INTERVAL.toString());
            interval.setSummary(interval.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.INTERVAL_CUSTOM.toString())) {
            EditTextPreference interval_custom = (EditTextPreference) findPreference(JuggerStonesApplication.PREFS.INTERVAL_CUSTOM.toString());
            interval_custom.setSummary(interval_custom.getText());
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.SOUND_STONE.toString())) {
            SoundPreferenceList sound = (SoundPreferenceList) findPreference(JuggerStonesApplication.PREFS.SOUND_STONE.toString());
            sound.setSummary(sound.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.SOUND_GONG.toString())) {
            SoundPreferenceList gong = (SoundPreferenceList) findPreference(JuggerStonesApplication.PREFS.SOUND_GONG.toString());
            gong.setSummary(gong.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApplication.PREFS.LANGUAGE.toString())) {
            ListPreference language = (ListPreference) findPreference(JuggerStonesApplication.PREFS.LANGUAGE.toString());
            language.setSummary(language.getEntry());
        }
    }
    //endregion

    private void changeLanguage(String language) {
        LocaleUtils.setLocale(new Locale(language));
        LocaleUtils.updateConfig(getApplication(), getResources().getConfiguration());
        Intent intent = new Intent(this, MyPreferenceActivity.class);
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                JuggerStonesApplication.increaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                JuggerStonesApplication.decreaseMusicVolume();
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