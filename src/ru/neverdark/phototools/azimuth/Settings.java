package ru.neverdark.phototools.azimuth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    public static boolean is24HourMode(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(
                context.getString(R.string.pref_24HourMode), false);
    }

    public static boolean isInternetTimeZone(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_timezone),
                true);
    }
}
