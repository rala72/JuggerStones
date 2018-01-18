package contador.piedras.jugger.model;

import android.content.Context;

import java.util.TimerTask;

@SuppressWarnings("WeakerAccess")
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
        if (mode != -1 && (stones == mode || stones == (mode * 2))) {
            // if it reaches mode or mode * 2 (2nd part) -> play gong
            sound.playGong(context);
            callback.onGongPlayed(stones);
        } else sound.playStone(context);
    }

    //region update
    public void updateStones(long stones) {
        this.stones = stones;
    }

    public void updateMode(long mode) {
        this.mode = mode;
    }
    //endregion

    public interface CounterTaskCallback {
        void onStonesChanged(long stones);

        void onGongPlayed(long stones);
    }
}