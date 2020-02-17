package io.rala.jugger;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.ContextThemeWrapper;

import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LocaleUtils {
    // public static final Locale DEFAULT_LOCALE = Locale.getDefault();
    // https://stackoverflow.com/a/36922319/2715720
    private static Locale sLocale = Locale.getDefault();

    public static void setLocale(Locale locale) {
        sLocale = locale;
        if (sLocale != null) Locale.setDefault(sLocale);
    }

    public static Locale getLocale() {
        return sLocale;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static void updateConfig(ContextThemeWrapper wrapper) {
        if (sLocale != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Configuration configuration = new Configuration();
            configuration.setLocale(sLocale);
            wrapper.applyOverrideConfiguration(configuration);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public static void updateConfig(Application app, Configuration configuration) {
        if (sLocale != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Wrapping the configuration to avoid Activity endless loop
            final Configuration config = new Configuration(configuration);
            config.locale = sLocale;
            final Resources res = app.getResources();
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
    }
}
