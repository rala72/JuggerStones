package contador.piedras.jugger;

import java.util.Locale;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;

public class AppPreferences extends PreferenceActivity {

	boolean changeLanguage = false;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		final ListPreference prefListLenguage = (ListPreference) findPreference("pref_lenguage");

		prefListLenguage
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						changeLanguage(newValue.toString());
						return true;

					}
				});
	}

	private void changeLanguage(String lenguage) {
		Locale newLocale = new Locale(lenguage.toLowerCase(Locale.US), lenguage);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = newLocale;
		res.updateConfiguration(conf, dm);
		Intent refresh = new Intent(this, AppPreferences.class);
		startActivity(refresh);
		finish();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent a = new Intent(this, MainActivity.class);
		startActivity(a);
		finish();
	}
}