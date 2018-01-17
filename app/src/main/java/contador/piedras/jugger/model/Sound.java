package contador.piedras.jugger.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class Sound {
    private String stone;
    private String gong;

    public Sound(String stoneSound, String gongSound) {
        this.stone = stoneSound;
        this.gong = gongSound;
    }

    public void playStone(Context context) {
        play(context, stone);
    }

    public void playGong(Context context) {
        play(context, gong);
    }

    private void play(Context context, String name) {
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + name);
        MediaPlayer auxStone = MediaPlayer.create(context, uri);
        auxStone.start();
        auxStone.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}
