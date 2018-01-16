package contador.piedras.jugger.model;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;

public class MyHandler extends Handler {
    private AppCompatTextView textView;
    private String message;

    MyHandler(AppCompatTextView textView) {
        this.textView = textView;
    }

    public void handleMessage(Message m) {
        this.textView.setText(this.message);
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    void refresh() {
        super.sendEmptyMessage(0);
    }
}