/*******************************************************************************
 * Copyright (C) 2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
