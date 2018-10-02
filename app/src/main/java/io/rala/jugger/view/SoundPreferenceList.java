package io.rala.jugger.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.ListPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import io.rala.jugger.JuggerStonesApplication;
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
            final String key = getArguments() != null ? getArguments().getString(ARG_KEY) : null;
            builder.setSingleChoiceItems(preference.getEntries(), clickedEntryIndex, (dialog, which) -> {
                clickedEntryIndex = which;
                String value = preference.getEntryValues()[which].toString();
                if (key.startsWith(JuggerStonesApplication.PREFS.SOUND_STONE.toString()))
                    new Sound(value, null).playStone(getContext());
                else if (key.equals(JuggerStonesApplication.PREFS.SOUND_GONG.toString()))
                    new Sound(null, value).playGong(getContext());
            });
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                preference.setValue(preference.getEntryValues()[clickedEntryIndex].toString());
                dialog.dismiss();
            });
        }
    }
}