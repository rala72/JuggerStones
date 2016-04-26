package contador.piedras.jugger;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class Hand extends Handler {
	String hcron;
	TextView tv_time;

	public Hand(TextView t) {
		this.tv_time = t;
	}

	public void handleMessage(Message msg) {
		tv_time.setText(hcron);
	}

	public void setHcron(String hcron) {
		this.hcron = hcron;
	}

	public void act() {
		super.sendEmptyMessage(0);
	}

}