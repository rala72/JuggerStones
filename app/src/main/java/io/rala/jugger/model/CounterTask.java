package io.rala.jugger.model;

import android.content.Context;

import java.util.TimerTask;

import io.rala.jugger.JuggerStonesApplication;

public class CounterTask extends TimerTask {
    private final Context context;
    private final long mode;
    private final Sound sound;
    private final CounterTaskCallback callback;
    private long stones;

    public CounterTask(Context context, long stones, long mode, Sound sound, CounterTaskCallback callback) {
        this.context = context;
        this.stones = stones;
        this.mode = mode;
        this.sound = sound;
        this.callback = callback;
    }

    public void run() {
        if (stones == Long.MAX_VALUE) return;
        if (JuggerStonesApplication.CounterPreference.isReverse()) stones--;
        else stones++;
        stones %= mode * 2;
        callback.onStonesChanged(stones);
        if (JuggerStonesApplication.CounterPreference.isReverse() && stones == 0 ||
                JuggerStonesApplication.CounterPreference.isNormalMode() && stones > 0 && stones % mode == 0) {
            stones %= mode;
            if (JuggerStonesApplication.CounterPreference.isReverse()) stones = JuggerStonesApplication.CounterPreference.getMode();
            sound.playGong(context);
            callback.onGongPlayed(stones);
        } else sound.playStone(context);
    }

    public interface CounterTaskCallback {
        void onStonesChanged(long stones);

        void onGongPlayed(long stones);
    }
}