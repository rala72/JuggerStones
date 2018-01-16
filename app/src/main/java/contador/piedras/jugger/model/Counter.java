package contador.piedras.jugger.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageButton;
import android.widget.TextView;

import contador.piedras.jugger.R;

public class Counter extends Thread {
    private long counter;
    private long mode;
    private long interval = 1500;
    private boolean stopped = false;
    private Sound sound;
    private MyHandler handler;
    private AppCompatImageButton button_playPause;
    private SharedPreferences sharedPreferences;

    public Counter(Context context, TextView textView, long counter, long mode, long interval, Sound sound, AppCompatImageButton button_playPause) {
        this.counter = counter;
        this.mode = mode;
        this.interval = interval;
        this.sound = sound;
        this.handler = new MyHandler(textView);
        this.button_playPause = button_playPause;
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
            counter += 1;
            handler.setMessage(counter + "");
            handler.refresh();
            try {
                if (counter == mode || counter == (mode * 2)) { // if it reaches mode or mode * 2 (2nd part) -> sounds gong
                    sound.playGong();
                    setStopped(sharedPreferences.getBoolean("stop_after_gong", false));
                    button_playPause.setBackgroundResource(R.drawable.play);
                } else sound.playStone();
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