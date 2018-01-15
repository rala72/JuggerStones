package contador.piedras.jugger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Assit extends Activity {
    private AudioManager audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assit);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Button b_msg = findViewById(R.id.B_msg);

        b_msg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent itSend = new Intent(Intent.ACTION_SEND);
                itSend.setType("plain/text");
                itSend.putExtra(Intent.EXTRA_EMAIL,
                        new String[]{"cristiancvacas@gmail.com"});
                itSend.putExtra(Intent.EXTRA_SUBJECT,
                        "Jugger Stones message");
                startActivity(itSend);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // Para controlar el volumen
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            default:
                return true; // should be false..?
        }
    }
}
