package contador.piedras.jugger.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class Sound {
    private String stone;
    private String gong;
    private Context context;

    public Sound(Context context, String stoneSound, String gongSound) {
        this.stone = stoneSound;
        this.gong = gongSound;
        this.context = context;
    }

    public void playStone() {
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + stone);
        MediaPlayer auxStone = MediaPlayer.create(context, uri);
        auxStone.start();
        auxStone.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }

    public void playGong() {
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + gong);
        MediaPlayer auxGong = MediaPlayer.create(context, uri);
        auxGong.start();
        auxGong.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}
