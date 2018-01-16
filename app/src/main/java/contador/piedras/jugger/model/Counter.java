package contador.piedras.jugger.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageButton;
import android.widget.TextView;

import contador.piedras.jugger.R;

public class Counter extends Thread {
    private long stones;
    private long mode;
    private long interval = 1500;
    private boolean stopped = false;
    private Sound sound;
    private MyHandler handler;
    private SharedPreferences sharedPreferences;
    private AppCompatImageButton button_playPause;

    public Counter(Context context, TextView textView, long stones, long mode, long interval, Sound sound, AppCompatImageButton button_playPause) {
        this.stones = stones;
        this.mode = mode;
        this.interval = interval;
        this.sound = sound;
        this.handler = new MyHandler(textView);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.button_playPause = button_playPause;
    }

    @SuppressWarnings("static-access")
    public void run() {
        try {
            this.sleep(interval);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        while (!stopped) {
            stones += 1;
            handler.setMessage(stones + "");
            handler.refresh();
            try {
                if (stones == mode || stones == (mode * 2)) { // if it reaches mode or mode * 2 (2nd part) -> sounds gong
                    sound.activateGong();
                    setStopped(sharedPreferences.getBoolean("stop_after_gong", false));
                    button_playPause.setBackgroundResource(R.drawable.play);
                } else sound.activateStone();
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