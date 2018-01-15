package contador.piedras.jugger;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class Sounds {

    private String stone;
    private String gong;
    private Context con;

    public Sounds(Context con, String stoneSound, String gongSound) {
        this.stone = stoneSound;
        this.gong = gongSound;
        this.con = con;
    }

    public void activateStone() {
        Uri uri = Uri.parse("android.resource://" + con.getPackageName() + "/raw/" + stone);
        MediaPlayer auxStone = MediaPlayer.create(con, uri);
        auxStone.start();
        auxStone.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }

    public void activateGong() {
        Uri uri = Uri.parse("android.resource://" + con.getPackageName() + "/raw/" + gong);
        MediaPlayer auxGong = MediaPlayer.create(con, uri);
        auxGong.start();
        auxGong.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}
