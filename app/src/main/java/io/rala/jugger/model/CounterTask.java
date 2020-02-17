package io.rala.jugger.model;

import android.content.Context;

import java.util.TimerTask;

import io.rala.jugger.JuggerStonesApp;

public class CounterTask extends TimerTask {
    private final Context context;
    private final long mode;
    private final Sound sound;
    private final CounterTaskCallback callback;
    private long stones;

    public CounterTask(Context context, long stones, long mode,
                       Sound sound, CounterTaskCallback callback) {
        this.context = context;
        this.stones = stones;
        this.mode = mode == 0 ? 1 : mode; // just to make sure
        this.sound = sound;
        this.callback = callback;
    }

    public void run() {
        if (Math.abs(stones) >= Long.MAX_VALUE)
            stones = JuggerStonesApp.CounterPreference.getModeStart();

        stones += JuggerStonesApp.CounterPreference.isReverse() ? -1 : 1;
        if (JuggerStonesApp.CounterPreference.isNormalModeIgnoringReverse()) stones %= mode * 2;

        callback.onStonesChanged(stones);
        if (JuggerStonesApp.CounterPreference.isReverse() && stones == 0 ||
            JuggerStonesApp.CounterPreference.isNormalMode() && stones == mode) {
            stones %= mode;
            if (JuggerStonesApp.CounterPreference.isReverse())
                stones = JuggerStonesApp.CounterPreference.getMode();
            sound.playGong(context);
            callback.onGongPlayed(stones);
        } else {
            if (JuggerStonesApp.CounterPreference.isStoneCountdown(stones))
                sound.playStoneCountdown(context);
            else sound.playStone(context);
        }
    }

    public interface CounterTaskCallback {
        void onStonesChanged(long stones);

        void onGongPlayed(long stones);
    }
}
