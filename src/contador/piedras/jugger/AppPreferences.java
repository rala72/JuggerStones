package contador.piedras.jugger;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AppPreferences extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
}