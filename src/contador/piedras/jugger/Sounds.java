package contador.piedras.jugger;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class Sounds {

	private int stone;
	private int gong;
    private Context con;
    
	public Sounds(Context con, int stoneSound, int gongSound){
		this.stone = stoneSound;
		this.gong  = gongSound;
		this.con   = con;
	}
	
	public void ActivateStone(){
		MediaPlayer auxStone = MediaPlayer.create(con, stone);
		auxStone.start();
		auxStone.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
            }
		});
	}
	public void ActivateGong(){
		MediaPlayer auxGong = MediaPlayer.create(con, gong);
		auxGong.start();
		auxGong.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
            }
		});
	}
	
	

	
}
