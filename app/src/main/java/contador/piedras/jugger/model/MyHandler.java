package contador.piedras.jugger.model;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class MyHandler extends Handler {
    private String message;
    private TextView tv_time;

    MyHandler(TextView t) {
        this.tv_time = t;
    }

    public void handleMessage(Message msg) {
        tv_time.setText(message);
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    void refresh() {
        super.sendEmptyMessage(0);
    }
}