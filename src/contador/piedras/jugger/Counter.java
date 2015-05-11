package contador.piedras.jugger;

import android.widget.TextView;

public class Counter extends Thread {
	int stones;
	int mode;
	boolean stoped = false;

	int interval = 1500;
	Sounds sounds;
	Hand handler;


	public Counter(TextView t, int stones, int mode, int interval, Sounds s) {

		this.stones = stones; // iguala las variables
		this.handler = new Hand(t);
		this.mode = mode;
		this.interval = interval;
		this.sounds = s;

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
				if (stones + 1 == mode || stones + 1 == (mode * 2)) {
					sounds.ActivateGong();
				} else {
					sounds.ActivateStone();
				}
				this.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isStoped() {
		return stoped;
	}

	public void setStoped(boolean stoped) {
		this.stoped = stoped;
	}

}