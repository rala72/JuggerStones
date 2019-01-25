package io.rala.jugger.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import io.rala.jugger.JuggerStonesApp;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Sound {
    private final String stone;
    private final String stoneCountdown;
    private final String gong;

    public Sound(String stone, String gong) {
        this(stone, null, gong);
    }

    public Sound(String stone, String stoneCountdown, String gong) {
        this.stone = stone != null ? stone : JuggerStonesApp.DEFAULT_STONE;
        this.stoneCountdown = stoneCountdown != null ? stoneCountdown : this.stone;
        this.gong = gong != null ? gong : JuggerStonesApp.DEFAULT_GONG;
    }

    public boolean playStone(Context context) {
        return play(context, stone);
    }

    public boolean playStoneCountdown(Context context) {
        return play(context, stoneCountdown);
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
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        return true;
    }
}
