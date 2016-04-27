package contador.piedras.jugger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

public class Counter extends Thread {
	int stones;
	int mode;
	boolean stoped = false;

	int interval = 1500;
	Sounds sounds;
	Hand handler;
    SharedPreferences SP;
    Button play;


    public Counter(TextView t, int stones, int mode, int interval, Sounds s, Context context, Button play) {

		this.stones = stones; // iguala las variables
		this.handler = new Hand(t);
		this.mode = mode;
		this.interval = interval;
		this.sounds = s;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.play =play;
    }

	@SuppressWarnings("static-access")
	public void run() {
		try {
			this.sleep(interval);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while (!stoped) {			
			stones += 1;
			handler.setHcron(stones + "");
			handler.act();
			try {
				if (stones == mode || stones == (mode * 2)) { //si llega a modo o modo*2 (2º parte) suena gong
					sounds.ActivateGong();
                    setStoped(SP.getBoolean("stop_after_gong", false));
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


	public void setStoped(boolean stoped) {
		this.stoped = stoped;
	}

}