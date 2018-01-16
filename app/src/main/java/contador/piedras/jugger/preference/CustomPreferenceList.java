package contador.piedras.jugger.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;

import contador.piedras.jugger.model.Sound;


public class CustomPreferenceList extends ListPreference implements OnClickListener {
    private int currentEntryIndex;

    public CustomPreferenceList(Context context, AttributeSet attrs) {
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
                Sound s = new Sound(getContext(), value, value);

                if (getKey().equals("time_sounds"))
                    s.activateStone();
                else if (getKey().equals("gong_sounds"))
                    s.activateGong();
            }
        });
        System.out.println(getEntry() + " " + this.getEntries()[0]);
        builder.setPositiveButton(android.R.string.ok, this);
    }

    public void onClick(DialogInterface dialog, int which) {
        this.setValue(this.getEntryValues()[currentEntryIndex] + "");
    }
}