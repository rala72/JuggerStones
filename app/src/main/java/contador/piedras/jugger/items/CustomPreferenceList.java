package contador.piedras.jugger.items;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;

import contador.piedras.jugger.R;
import contador.piedras.jugger.Sounds;


public class CustomPreferenceList extends ListPreference implements OnClickListener {

    private int mClickedDialogEntryIndex;
    private Context context;

    public CustomPreferenceList(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    private int getValueIndex() {
        return findIndexOfValue(this.getValue() + "");
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mClickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(this.getEntries(), mClickedDialogEntryIndex, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mClickedDialogEntryIndex = which;
                int rawSound = Integer.parseInt((getEntryValues()[which]).toString());
                Sounds s = new Sounds(context, rawSound, rawSound);

                if (getKey().equals("time_sounds"))
                    s.activateStone();
                else if (getKey().equals("gong_sounds"))
                    s.activateGong();
            }
        });
        System.out.println(getEntry() + " " + this.getEntries()[0]);
        builder.setPositiveButton(R.string.accept, this);
    }

    public void onClick(DialogInterface dialog, int which) {
        this.setValue(this.getEntryValues()[mClickedDialogEntryIndex] + "");
    }
}