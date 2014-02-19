package ru.neverdark.phototools.azimuth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class for application settings
 */
public class Settings {
    /**
     * Checks value for 24-hour mode
     * 
     * @param context
     *            application context
     * @return true for enabled 24-hour mode
     */
    public static boolean is24HourMode(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(
                context.getString(R.string.pref_24HourMode), false);
    }

    /**
     * Checks value for "Internet time zone"
     * 
     * @param context
     *            application context
     * @return true for enabled "Internet time zone"
     */
    public static boolean isInternetTimeZone(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_timezone),
                true);
    }

    /**
     * Checks value for "Show tip"
     * 
     * @param context
     *            application context
     * @return true for enabled "Show tip"
     */
    public static boolean isShowTip(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_tip),
                true);
    }
}
