package contador.piedras.jugger.model;

import android.content.Context;

import java.util.TimerTask;

public class CounterTask extends TimerTask {
    private Context context;
    private long stones;
    private long mode;
    private Sound sound;
    private CounterTaskCallback callback;

    public CounterTask(Context context, long stones, long mode, Sound sound, CounterTaskCallback callback) {
        this.context = context;
        this.stones = stones;
        this.mode = mode;
        this.sound = sound;
        this.callback = callback;
    }

    public void run() {
        if (stones == Long.MAX_VALUE) return;
        stones += 1;
        callback.onStonesChanged(stones);
        if (mode != -1 && stones % mode == 0) {
            sound.playGong(context);
            callback.onGongPlayed(stones);
        } else sound.playStone(context);
    }

    public interface CounterTaskCallback {
        void onStonesChanged(long stones);

        void onGongPlayed(long stones);
    }
}