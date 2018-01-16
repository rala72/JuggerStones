package contador.piedras.jugger.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import contador.piedras.jugger.R;

public class Counter extends Thread {
    private int stones;
    private int mode;
    private boolean stopped = false;

    private int interval = 1500;
    private Sound sound;
    private MyHandler handler;
    private SharedPreferences SP;
    private Button play;

    public Counter(TextView t, int stones, int mode, int interval, Sound s, Context context, Button play) {
        this.stones = stones; // iguala las variables
        this.handler = new MyHandler(t);
        this.mode = mode;
        this.interval = interval;
        this.sound = s;
        this.SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.play = play;
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
                if (stones == mode || stones == (mode * 2)) { //si llega a modo o modo*2 (2º parte) suena gong
                    sound.activateGong();
                    setStopped(SP.getBoolean("stop_after_gong", false));
                    play.setBackgroundResource(R.drawable.play);
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