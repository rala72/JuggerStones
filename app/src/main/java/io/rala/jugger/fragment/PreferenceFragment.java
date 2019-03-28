package io.rala.jugger.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import net.xpece.android.support.preference.EditTextPreference;
import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.XpPreferenceFragment;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import io.rala.jugger.JuggerStonesApp;
import io.rala.jugger.LocaleUtils;
import io.rala.jugger.MainActivity;
import io.rala.jugger.R;
import io.rala.jugger.model.InputFilterMinMaxDecimal;
import io.rala.jugger.model.InputFilterMinMaxInteger;
import io.rala.jugger.model.Team;
import io.rala.jugger.view.SoundPreferenceList;

public class PreferenceFragment extends XpPreferenceFragment implements MainActivity.OnBackPressedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static PreferenceFragment newInstance(long stones, Team team1, Team team2) {
        PreferenceFragment fragment = new PreferenceFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(MainActivity.KEY_COUNTER, stones);
        bundle.putParcelable(MainActivity.KEY_TEAM1, team1);
        bundle.putParcelable(MainActivity.KEY_TEAM2, team2);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (JuggerStonesApp.CounterPreference.isKeepDisplayAwake())
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.settings));
        ((MainActivity) getActivity()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        initFilter();
        initListener();
        updatePreferencesEnabled(null);
        updateLanguageText(); // has to be before sumTexts!
        updateSumTexts(null);
    }

    private void initFilter() {
        EditTextPreference mode_custom = (EditTextPreference) findPreference(JuggerStonesApp.PREFS.MODE_CUSTOM.toString());
        mode_custom.setOnEditTextCreatedListener(editText -> {
            editText.setFilters(new InputFilter[]{new InputFilterMinMaxInteger(1)});
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        });
        EditTextPreference interval_custom = (EditTextPreference) findPreference(JuggerStonesApp.PREFS.INTERVAL_CUSTOM.toString());
        interval_custom.setOnEditTextCreatedListener(editText -> {
            editText.setFilters(new InputFilter[]{new InputFilterMinMaxDecimal(1)});
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        });
    }

    private void initListener() {
        final ListPreference pref_language = (ListPreference) findPreference(JuggerStonesApp.PREFS.LANGUAGE.toString());
        pref_language.setDefaultValue(Locale.getDefault().getLanguage());
        pref_language.setValue(LocaleUtils.getLocale().getLanguage());
        pref_language.setOnPreferenceChangeListener((preference, newValue) -> {
            ((MainActivity) getActivity()).changeLanguage(newValue.toString());
            return true;
        });

        final Preference pref_email = findPreference(JuggerStonesApp.PREFS.EMAIL.toString());
        pref_email.setOnPreferenceClickListener(preference -> {
            JuggerStonesApp.sendEmail(getActivity());
            return true;
        });
        final Preference pref_version = findPreference(JuggerStonesApp.PREFS.VERSION.toString());
        pref_version.setTitle(getString(R.string.pref_version, JuggerStonesApp.getVersion(getActivity())));
        pref_version.setOnPreferenceClickListener(preference -> {
            final String appPackageName = getActivity().getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setFocusable(false);

        setDivider(null);
        getListView().addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBetweenItems(false));
    }

    @Override
    public void onBackPressed() {
        goToMainFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goToMainFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(JuggerStonesApp.PREFS.SOUND_STONE.toString())) updateSoundStoneCountdown();
        validatePreferences(key);
        updatePreferencesEnabled(key);
        updateSumTexts(key);
    }

    //region updatePrefs
    private void updateSoundStoneCountdown() {
        final String value = ((SoundPreferenceList) findPreference(JuggerStonesApp.PREFS.SOUND_STONE.toString())).getValue();
        ((SoundPreferenceList) findPreference(JuggerStonesApp.PREFS.SOUND_STONE_COUNTDOWN.toString())).setValue(value);
    }

    private void validatePreferences(String key) {
        if (key.equals(JuggerStonesApp.PREFS.MODE_CUSTOM.toString())) {
            long min = 1;
            EditTextPreference mode_custom = (EditTextPreference) findPreference(JuggerStonesApp.PREFS.MODE_CUSTOM.toString());
            long value = mode_custom.getText().trim().isEmpty() ? min : Long.parseLong(mode_custom.getText().trim());
            if (value == 0 || value == min) mode_custom.setText(String.valueOf(value));
        }
        if (key.equals(JuggerStonesApp.PREFS.INTERVAL_CUSTOM.toString())) {
            double min = 0.001;
            EditTextPreference interval_custom = (EditTextPreference) findPreference(JuggerStonesApp.PREFS.INTERVAL_CUSTOM.toString());
            double value = interval_custom.getText().trim().isEmpty() ? min : Double.parseDouble(interval_custom.getText().trim());
            if (value == 0 || value == min) interval_custom.setText(String.valueOf(value));
        }
    }

    private void updatePreferencesEnabled(String key) {
        if (key == null || key.equals(JuggerStonesApp.PREFS.MODE.toString())) {
            ListPreference mode = (ListPreference) findPreference(JuggerStonesApp.PREFS.MODE.toString());
            findPreference(JuggerStonesApp.PREFS.MODE_CUSTOM.toString()).setEnabled(mode.getValue().equals("0"));
            findPreference(JuggerStonesApp.PREFS.REVERSE.toString()).setEnabled(!mode.getValue().equals("-1"));
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.INTERVAL.toString())) {
            ListPreference interval = (ListPreference) findPreference(JuggerStonesApp.PREFS.INTERVAL.toString());
            findPreference(JuggerStonesApp.PREFS.INTERVAL_CUSTOM.toString()).setEnabled(interval.getValue().equals("0"));
        }
    }

    private void updateLanguageText() {
        if (LocaleUtils.getLocale().equals(Locale.ENGLISH)) return;
        final String format = "%s (%s)";
        final ListPreference pref_language = (ListPreference) findPreference(JuggerStonesApp.PREFS.LANGUAGE.toString());
        pref_language.setTitle(String.format(format, getString(R.string.language), getString(R.string.language_en)));
        // region languageMap
        Map<String, String> languageMap = new TreeMap<>();
        languageMap.put(getString(R.string.language_english), getString(R.string.language_english_en));
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

    private void updateSumTexts(String key) {
        if (key == null || key.equals(JuggerStonesApp.PREFS.MODE.toString())) {
            ListPreference mode = (ListPreference) findPreference(JuggerStonesApp.PREFS.MODE.toString());
            mode.setSummary(mode.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.MODE_CUSTOM.toString())) {
            EditTextPreference mode_custom = (EditTextPreference) findPreference(JuggerStonesApp.PREFS.MODE_CUSTOM.toString());
            mode_custom.setSummary(mode_custom.getText());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.INTERVAL.toString())) {
            ListPreference interval = (ListPreference) findPreference(JuggerStonesApp.PREFS.INTERVAL.toString());
            interval.setSummary(interval.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.INTERVAL_CUSTOM.toString())) {
            EditTextPreference interval_custom = (EditTextPreference) findPreference(JuggerStonesApp.PREFS.INTERVAL_CUSTOM.toString());
            interval_custom.setSummary(interval_custom.getText());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.SOUND_STONE.toString())) {
            SoundPreferenceList sound = (SoundPreferenceList) findPreference(JuggerStonesApp.PREFS.SOUND_STONE.toString());
            sound.setSummary(sound.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.SOUND_STONE.toString()) || key.equals(JuggerStonesApp.PREFS.SOUND_STONE_COUNTDOWN.toString())) {
            SoundPreferenceList sound_countdown = (SoundPreferenceList) findPreference(JuggerStonesApp.PREFS.SOUND_STONE_COUNTDOWN.toString());
            sound_countdown.setSummary(sound_countdown.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.SOUND_GONG.toString())) {
            SoundPreferenceList gong = (SoundPreferenceList) findPreference(JuggerStonesApp.PREFS.SOUND_GONG.toString());
            gong.setSummary(gong.getEntry());
        }
        if (key == null || key.equals(JuggerStonesApp.PREFS.LANGUAGE.toString())) {
            ListPreference language = (ListPreference) findPreference(JuggerStonesApp.PREFS.LANGUAGE.toString());
            language.setSummary(language.getEntry());
        }
    }
    //endregion

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof SoundPreferenceList) {
            DialogFragment fragment = SoundPreferenceList.SoundPreferenceListFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else super.onDisplayPreferenceDialog(preference);
    }

    private void goToMainFragment() {
        final long stones = getArguments() != null ?
            getArguments().getLong(MainActivity.KEY_COUNTER, JuggerStonesApp.CounterPreference.getModeStart()) :
            JuggerStonesApp.CounterPreference.getModeStart();
        final Team team1 = getArguments() != null ?
            (Team) getArguments().getParcelable(MainActivity.KEY_TEAM1) : null;
        final Team team2 = getArguments() != null ?
            (Team) getArguments().getParcelable(MainActivity.KEY_TEAM2) : null;
        ((MainActivity) getActivity()).goToMainFragment(stones, team1, team2);
    }

    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}