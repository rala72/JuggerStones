package io.rala.jugger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import io.rala.jugger.model.HistoryEntry;
import io.rala.jugger.model.Sound;

public class JuggerStonesApp extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static SharedPreferences sharedPreferences;
    public static AudioManager audioManager;
    public static Sound sound;

    private static final List<HistoryEntry> history = new ArrayList<>();

    public static final long DEFAULT_MODE = 100;
    public static final long DEFAULT_INTERVAL = 1500;
    public static final String DEFAULT_STONE = "stone";
    public static final String DEFAULT_GONG = "gong";

    public enum PREFS { // see also preference.xml // some of them are only for findPreference
        MODE("mode"), MODE_PREVIOUS("mode_previous"),
        MODE_CUSTOM("mode_custom"),
        INTERVAL("interval"), INTERVAL_CUSTOM("interval_custom"),
        REVERSE("reverse"), IMMEDIATE_START("immediateStart"),
        STOP_AFTER_POINT("stop_after_point"), STOP_AFTER_GONG("stop_after_gong"),
        SOUND_STONE("sound_stone"), SOUND_STONE_COUNTDOWN("sound_stone_countdown"),
        SOUND_GONG("sound_gong"),
        KEEP_DISPLAY_AWAKE("keep_display_awake"), LANGUAGE("language"),
        EMAIL("email"), VERSION("version");

        private final String text;

        PREFS(String text) {
            this.text = text;
        }

        @NonNull
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
        changeLanguageIfNotDefault();
        updateSound();
    }

    //region changeLanguage
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateConfig(this, newConfig);
    }

    private void changeLanguageIfNotDefault() {
        final String language_default = Locale.getDefault().getLanguage();
        final String language_pref =
            sharedPreferences.getString(PREFS.LANGUAGE.toString(), language_default);
        if (!new Locale(language_default).getLanguage().equals(language_pref)) {
            LocaleUtils.setLocale(new Locale(language_pref));
            LocaleUtils.updateConfig(this, getResources().getConfiguration());
        }
    }
    //endregion

    //region sound & volume
    public static void updateSound() {
        updateSound(null, null, null);
    }

    @SuppressWarnings("SameParameterValue")
    public static void updateSound(String stone, String stoneCountdown, String gong) {
        if (stone == null)
            stone = sharedPreferences.getString(PREFS.SOUND_STONE.toString(), DEFAULT_STONE);
        if (stoneCountdown == null)
            stoneCountdown = sharedPreferences.getString(
                PREFS.SOUND_STONE_COUNTDOWN.toString(), stone
            );
        if (gong == null)
            gong = sharedPreferences.getString(PREFS.SOUND_GONG.toString(), DEFAULT_GONG);
        sound = new Sound(stone, stoneCountdown, gong);
    }

    public static void increaseMusicVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
        );
    }

    public static void decreaseMusicVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
        );
    }
    //endregion

    //region history

    /**
     * saves entry to history if not already in
     */
    public static void saveToHistory(HistoryEntry entry) {
        if (!history.contains(entry)) history.add(entry);
    }

    /**
     * @return last {@link HistoryEntry} and removes it
     */
    public static HistoryEntry getLastHistoryEntry() {
        if (history.isEmpty()) return null;
        HistoryEntry entry = history.get(history.size() - 1);
        history.remove(history.size() - 1);
        return entry;
    }

    public static void clearHistory() {
        history.clear();
    }
    //endregion

    //region version & email
    public static String getVersion(Context context) {
        try {
            PackageInfo pInfo =
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getVersionCode(Context context) {
        try {
            PackageInfo pInfo =
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(pInfo.versionCode);
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
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL,
            new String[]{context.getString(R.string.email_current)}
        );
        intent.putExtra(Intent.EXTRA_SUBJECT,
            context.getString(R.string.app_name) + JuggerStonesApp.getVersionForEmail(context)
        );
        intent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(intent);
    }
    //endregion

    //region preferences
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith(PREFS.SOUND_STONE.toString()) ||
            key.equals(PREFS.SOUND_GONG.toString()))
            updateSound();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class CounterPreference {
        /**
         * @return current mode number
         * @see #getModeMax()
         * @see #getModeMin()
         * @see #getModeStart()
         */
        public static long getMode() {
            long mode = getLongFromString(PREFS.MODE, DEFAULT_MODE);
            if (mode == 0)
                mode = getLongFromString(PREFS.MODE_CUSTOM, DEFAULT_MODE);
            return mode;
        }

        /**
         * @return current mode min number
         * @see #getMode()
         * @see #getModeMax()
         * @see #getModeStart()
         */
        public static long getModeMin() {
            return isInfinityMode() || isReverse() ? 0 : -getMode() + 1;
        }

        /**
         * @return current mode min number
         * @see #getMode()
         * @see #getModeMin()
         * @see #getModeStart()
         */
        public static long getModeMax() {
            return isInfinityMode() ? Long.MAX_VALUE :
                isReverse() ? getMode() * 2 - 1 : getMode() - 1;
        }

        /**
         * @return current mode start number <i>(for stop)</i>
         * @see #getMode()
         * @see #getModeMax()
         * @see #getModeMin()
         */
        public static long getModeStart() {
            return isInfinityMode() || isNormalMode() ? 0 : getMode();
        }

        /**
         * toggles between infinity and other mode
         *
         * @return toggled mode number
         * @see #getMode()
         */
        public static long getPreviousMode() {
            long previous = getLongFromString(PREFS.MODE_PREVIOUS, DEFAULT_MODE);
            if (previous == getMode() || (previous != -1 && !isInfinityMode()))
                if (isInfinityMode()) previous = DEFAULT_MODE;
                else previous = -1;
            return previous;
        }

        /**
         * @return interval between two stones
         */
        public static long getInterval() {
            long interval = getLongFromString(PREFS.INTERVAL, DEFAULT_INTERVAL);
            if (interval == 0)
                interval = new BigDecimal(
                    getLongFromString(PREFS.INTERVAL_CUSTOM, DEFAULT_INTERVAL)
                ).multiply(BigDecimal.valueOf(1000)).longValue();
            return interval <= 0 ? 1 : interval; // just to make sure
        }

        /**
         * @see #isNormalModeIgnoringReverse()
         * @see #isInfinityMode()
         * @see #isReverse()
         */
        public static boolean isNormalMode() {
            return isNormalMode(false);
        }

        /**
         * @see #isNormalMode()
         * @see #isInfinityMode()
         * @see #isReverse()
         */
        public static boolean isNormalModeIgnoringReverse() {
            return isNormalMode(true);
        }

        /**
         * use {@link #isNormalMode()} or {@link #isNormalModeIgnoringReverse()}
         * or {@link #isReverse()} instead
         *
         * @see #isNormalMode()
         * @see #isNormalModeIgnoringReverse()
         * @see #isInfinityMode()
         * @see #isReverse()
         */
        private static boolean isNormalMode(boolean ignoreReverse) {
            return !isInfinityMode() && (ignoreReverse || !isReverse());
        }

        /**
         * @see #isNormalMode()
         * @see #isNormalModeIgnoringReverse()
         * @see #isReverse()
         */
        public static boolean isInfinityMode() {
            return getMode() == -1;
        }

        /**
         * @see #isNormalMode()
         * @see #isNormalModeIgnoringReverse()
         * @see #isInfinityMode()
         */
        public static boolean isReverse() {
            return isNormalModeIgnoringReverse() &&
                sharedPreferences.getBoolean(PREFS.REVERSE.toString(), false);
        }

        public static boolean isImmediateStart() {
            return sharedPreferences.getBoolean(PREFS.IMMEDIATE_START.toString(), false);
        }

        public static boolean isStopAfterPoint() {
            return sharedPreferences.getBoolean(PREFS.STOP_AFTER_POINT.toString(), false);
        }

        public static boolean isStopAfterGong() {
            return sharedPreferences.getBoolean(PREFS.STOP_AFTER_GONG.toString(), false);
        }

        public static boolean isKeepDisplayAwake() {
            return sharedPreferences.getBoolean(PREFS.KEEP_DISPLAY_AWAKE.toString(), false);
        }

        public static boolean isStoneCountdown(long stones) {
            return isStoneCountdown(stones, 10);
        }

        public static boolean isStoneCountdown(long stones, long limit) {
            // TODO: make preference for 'limit' parameter
            return !isInfinityMode()
                && (isNormalMode() && limit > getMode() - stones
                || isReverse() && limit > stones);
        }

        private static long getLongFromString(PREFS pref, long defaultValue) {
            String string = sharedPreferences.getString(
                pref.toString(), String.valueOf(defaultValue)
            );
            return string == null ? defaultValue : Long.parseLong(string);
        }
    }
    //endregion
}
