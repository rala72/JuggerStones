package contador.piedras.jugger.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.widget.Toast;

import contador.piedras.jugger.R;

public class Counter extends Thread {
    private long stones;
    private long mode;
    private long interval = 1500;
    private boolean stopped = false;
    private Sound sound;
    private MyHandler handler;
    private AppCompatImageButton button_playPause;
    private Context context;
    private SharedPreferences sharedPreferences;

    public Counter(Context context, AppCompatTextView textView, long stones, long mode, long interval, Sound sound, AppCompatImageButton button_playPause) {
        if (stones == Long.MAX_VALUE) stones = 0;
        this.stones = stones;
        this.mode = mode;
        this.interval = interval;
        this.sound = sound;
        this.handler = new MyHandler(textView);
        this.button_playPause = button_playPause;
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @SuppressWarnings("static-access")
    public void run() {
        try {
            this.sleep(interval);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        while (!stopped) {
            if (stones == Long.MAX_VALUE) setStopped(true);
            stones += 1;
            handler.setMessage(stones + "");
            handler.refresh();
            try {
                if (mode != -1 && (stones == mode || stones == (mode * 2))) {
                    // if it reaches mode or mode * 2 (2nd part) -> play gong
                    sound.playGong();
                    setStopped(sharedPreferences.getBoolean("stop_after_gong", false));
                    button_playPause.setBackgroundResource(R.drawable.play);
                } else {
                    if (mode == -1 && stones % 100 == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.toast_infinity, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    sound.playStone();
                }
                this.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}