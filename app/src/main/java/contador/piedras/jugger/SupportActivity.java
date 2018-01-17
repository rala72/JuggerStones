package contador.piedras.jugger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Button button_sendMessage = findViewById(R.id.button);
        button_sendMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                JuggerStonesApplication.sendEmail(getApplicationContext());
            }
        });
    }
}
