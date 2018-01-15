package contador.piedras.jugger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

public class Counter extends Thread {
    private int stones;
    private int mode;
    private boolean stopped = false;

    private int interval = 1500;
    private Sounds sounds;
    private Hand handler;
    private SharedPreferences SP;
    private Button play;

    Counter(TextView t, int stones, int mode, int interval, Sounds s, Context context, Button play) {
        this.stones = stones; // iguala las variables
        this.handler = new Hand(t);
        this.mode = mode;
        this.interval = interval;
        this.sounds = s;
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
            handler.setHcron(stones + "");
            handler.act();
            try {
                if (stones == mode || stones == (mode * 2)) { //si llega a modo o modo*2 (2º parte) suena gong
                    sounds.ActivateGong();
                    setStopped(SP.getBoolean("stop_after_gong", false));
                    play.setBackgroundResource(R.drawable.play);
                } else {
                    sounds.ActivateStone();
                }
                this.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}