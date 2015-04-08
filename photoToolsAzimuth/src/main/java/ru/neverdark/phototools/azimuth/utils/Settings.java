/*******************************************************************************
 * Copyright (C) 2014-2015 Artem Yankovskiy (artemyankovskiy@gmail.com).
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
package ru.neverdark.phototools.azimuth.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import ru.neverdark.phototools.azimuth.R;

/**
 * Class for application settings
 */
public class Settings {
    private static final String IS_MAP_SAVED = "isMapSaved";
    private static final String MAP_CAMERA_LATITUDE = "mapCameraLatitude";
    private static final String MAP_CAMERA_LONGITUDE = "mapCameraLongitude";
    private static final String MAP_CAMERA_ZOOM = "mapCameraZoom";
    private static final String MAP_TYPE = "mapType";

    /**
     * Checks value for 24-hour mode
     *
     * @param context application context
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
     * @param context application context
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
     * @param context application context
     * @return true for enabled "Show tip"
     */
    public static boolean isShowTip(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_tip),
                true);
    }

    /**
     * Checks value for "Sunset"
     *
     * @param context
     * @return true for enabled "Sunset"
     */
    public static boolean isSunsetShow(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_showSunsetAzimuth),
                true);
    }

    /**
     * Checks value for "Sunrise"
     *
     * @param context
     * @return true for enabled "Sunrise"
     */
    public static boolean isSunriseShow(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_showSunriseAzimuth),
                true);
    }

    public static boolean isMapSaved(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(IS_MAP_SAVED, false);
    }

    public static LatLng getMapCameraPos(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double latitude = prefs.getFloat(MAP_CAMERA_LATITUDE, 0);
        double longitude = prefs.getFloat(MAP_CAMERA_LONGITUDE, 0);
        return new LatLng(latitude, longitude);
    }

    public static float getMapCameraZoom(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(MAP_CAMERA_ZOOM, 0);
    }

    public static void saveMapCameraZoom(Context context, float zoom) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putFloat(MAP_CAMERA_ZOOM, zoom).commit();
    }

    public static void saveMapCameraPosition(Context context, LatLng target) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().
                putFloat(MAP_CAMERA_LATITUDE, (float) target.latitude).
                putFloat(MAP_CAMERA_LONGITUDE, (float) target.longitude).
                commit();
    }

    public static void saveMap(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(IS_MAP_SAVED, true).commit();
    }

    public static void saveMapType(Context context, int mapType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(MAP_TYPE, mapType).commit();
    }

    public static int getMapType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
    }

    public static boolean isAltitudeShow(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_showAltitude),
                false);
    }
}
