package contador.piedras.jugger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import contador.piedras.jugger.model.Sound;

public class JuggerStonesApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static SharedPreferences sharedPreferences;
    public static AudioManager audioManager;
    public static Sound sound;

    public static final long DEFAULT_MODE = 100;
    public static final long DEFAULT_INTERVAL = 1500;

    public enum PREFS { // see also preference.xml // some of them are only for findPreference
        MODE("mode"), INTERVAL("interval"),
        STOP_AFTER_POINT("stop_after_point"), STOP_AFTER_GONG("stop_after_gong"),
        SOUND_STONE("sound_stone"), SOUND_GONG("sound_gong"),
        LANGUAGE("language"), CONTACT("contact"), PLAY_STORE("playStore");

        private final String text;

        PREFS(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateSound();
    }

    //region sound & volume
    public static void updateSound() {
        updateSound(null, null);
    }

    @SuppressWarnings("SameParameterValue")
    public static void updateSound(String soundStone, String soundGong) {
        if (soundStone == null)
            soundStone = JuggerStonesApplication.sharedPreferences.getString(JuggerStonesApplication.PREFS.SOUND_STONE.toString(), "stone");
        if (soundGong == null)
            soundGong = JuggerStonesApplication.sharedPreferences.getString(JuggerStonesApplication.PREFS.SOUND_GONG.toString(), "gong");
        sound = new Sound(soundStone, soundGong);
    }

    public static void increaseVolume() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public static void decreaseVolume() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }
    //endregion

    //region version & email
    public static String getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getVersionForEmail(Context context) {
        String version_txt = getVersion(context);
        String version_code = getVersionCode(context);

        if (version_txt == null || version_code == null)
            return context.getString(R.string.error_version_loading_failed);

        return ":" + version_txt + ":" + version_code;
    }

    public static void sendEmail(Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        //noinspection SpellCheckingInspection
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getString(R.string.email_current)});
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + JuggerStonesApplication.getVersionForEmail(context));
        intent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(intent);
    }
    //endregion

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREFS.SOUND_STONE.toString()) || key.equals(PREFS.SOUND_GONG.toString()))
            updateSound();
    }
}
