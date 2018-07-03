package io.rala.jugger.view;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;

import io.rala.jugger.JuggerStonesApplication;
import io.rala.jugger.model.Sound;

// note: values are stored as String
public class SoundPreferenceList extends ListPreference implements OnClickListener {
    private int currentEntryIndex;

    public SoundPreferenceList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        currentEntryIndex = findIndexOfValue(this.getValue());
        builder.setSingleChoiceItems(this.getEntries(), currentEntryIndex, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                currentEntryIndex = which;
                String value = (getEntryValues()[which]).toString();
                Sound s = new Sound(value, value);

                if (getKey().equals(JuggerStonesApplication.PREFS.SOUND_STONE.toString()))
                    s.playStone(getContext());
                else if (getKey().equals(JuggerStonesApplication.PREFS.SOUND_GONG.toString()))
                    s.playGong(getContext());
            }
        });
        System.out.println(getEntry() + " " + this.getEntries()[0]);
        builder.setPositiveButton(android.R.string.ok, this);
    }

    public void onClick(DialogInterface dialog, int which) {
        this.setValue(this.getEntryValues()[currentEntryIndex] + "");
    }
}