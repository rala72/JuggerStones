package io.rala.jugger.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import io.rala.jugger.JuggerStonesApp;
import io.rala.jugger.model.Sound;

// note: values are stored as String
// https://stackoverflow.com/a/32812614/2715720
public class SoundPreferenceList extends ListPreference {
    public SoundPreferenceList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static class SoundPreferenceListFragment extends ListPreferenceDialogFragmentCompat {
        private SoundPreferenceList preference;
        private int clickedEntryIndex;

        public SoundPreferenceListFragment() {
        }

        public static SoundPreferenceListFragment newInstance(Preference preference) {
            SoundPreferenceListFragment fragment = new SoundPreferenceListFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(ARG_KEY, preference.getKey());
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            preference = (SoundPreferenceList) getPreference();
            clickedEntryIndex = preference.findIndexOfValue(preference.getValue());
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            // super.onPrepareDialogBuilder(builder);
            final String key = getArguments() != null ?
                getArguments().getString(ARG_KEY) : null;
            builder.setSingleChoiceItems(
                preference.getEntries(), clickedEntryIndex,
                (dialog, which) -> {
                    clickedEntryIndex = which;
                    if (key == null) return;
                    String value = preference.getEntryValues()[which].toString();
                    if (key.startsWith(JuggerStonesApp.PREFS.SOUND_STONE.toString()))
                        new Sound(value, null).playStone(getContext());
                    else if (key.equals(JuggerStonesApp.PREFS.SOUND_GONG.toString()))
                        new Sound(null, value).playGong(getContext());
                });
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                preference.setValue(preference.getEntryValues()[clickedEntryIndex].toString());
                dialog.dismiss();
            });
        }
    }
}
