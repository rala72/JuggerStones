package contador.piedras.jugger;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class Hand extends Handler {
    private String hcron;
    private TextView tv_time;

    Hand(TextView t) {
        this.tv_time = t;
    }

    public void handleMessage(Message msg) {
        tv_time.setText(hcron);
    }

    void setHcron(String hcron) {
        this.hcron = hcron;
    }

    void act() {
        super.sendEmptyMessage(0);
    }
}