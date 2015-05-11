package contador.piedras.jugger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Assit extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_assit);
		
		Button b_msg = (Button)findViewById(R.id.B_msg);
		
		b_msg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent itSend = new Intent(android.content.Intent.ACTION_SEND);
				itSend.setType("plain/text");
				itSend.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { "cristiancvacas@gmail.com" });
				itSend.putExtra(android.content.Intent.EXTRA_SUBJECT,"Jugger Stones message");
				startActivity(itSend);				
			}
		});
	}

}
