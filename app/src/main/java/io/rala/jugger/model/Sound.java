package io.rala.jugger.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Sound {
    private String stone;
    private String gong;

    public Sound(String stoneSound, String gongSound) {
        this.stone = stoneSound;
        this.gong = gongSound;
    }

    public boolean playStone(Context context) {
        return play(context, stone);
    }

    public boolean playGong(Context context) {
        return play(context, gong);
    }

    private boolean play(Context context, String name) {
        final Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + name);
        final MediaPlayer mediaPlayer = MediaPlayer.create(context, uri);
        if (mediaPlayer == null) {
            Log.e(Sound.class.getSimpleName(), name + " not found.");
            return false;
        }
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        return true;
    }
}
