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
package ru.neverdark.phototools.azimuth.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ru.neverdark.phototools.azimuth.utils.Constants;
import ru.neverdark.phototools.azimuth.utils.Log;

/**
 * Class for gets a time zone from the Google
 */
public class GoogleTimeZone {
    private TimeZone mTimeZone;
    private Context mContext;
    private LatLng mLocation;
    private Calendar mCalendar;

    /**
     * Constructor
     *
     * @param context application context
     */
    public GoogleTimeZone(Context context) {
        mContext = context;
    }

    /**
     * Gets time zone
     *
     * @return time zone
     */
    public TimeZone getTimeZone() {
        return mTimeZone;
    }

    /**
     * Checks connection status
     *
     * @return true if device online, false in other case
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Reads TimeZone from Google Json
     *
     * @return TimeZone JSON from Google Json or empty if cannot determine
     */
    private String readTimeZoneJson() {
        StringBuilder builder = new StringBuilder();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(year, month, day, hour, minute);

        /* Gets desired time as seconds since midnight, January 1, 1970 UTC */
        Long timestamp = calendar.getTimeInMillis() / 1000;

        String url_format = "https://maps.googleapis.com/maps/api/timezone/json?location=%f,%f&timestamp=%d";
        String url = String.format(Locale.US, url_format, mLocation.latitude,
                mLocation.longitude, timestamp);

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 4000); // 4 sec
        HttpConnectionParams.setSoTimeout(params, 1000); // 1 sec

        HttpClient client = new DefaultHttpClient(params);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setParams(params);

        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.message("Download fail");
            }
        } catch (Exception e) {
            // request new address for object
            // creating new object is a faster then setLength(0)
            builder = new StringBuilder();
        }
        return builder.toString();
    }

    /**
     * Request time zone from google
     *
     * @return STATUS_SUCCESS if time zone was gets successfully, STATUS_FAIL in
     * other case
     */
    public int requestTimeZone() {
        int requestStatus = Constants.STATUS_FAIL;

        /* we have internet, download json from timeZone google service */
        if (isOnline()) {
            Log.message("Get Time Zone from Google");
            String json = readTimeZoneJson();
            Log.variable("json", json);

            /* JSON data not empty, parse it */
            if (json.length() != 0) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String timeZoneId = jsonObject.getString("timeZoneId");
                    String rawOffset = jsonObject.getString("rawOffset");
                    Log.variable("timeZoneId", timeZoneId);
                    Log.variable("rawOffset", rawOffset);
                    mTimeZone = TimeZone.getTimeZone(timeZoneId);
                    mTimeZone.setRawOffset(Integer.valueOf(rawOffset) * 1000);
                    requestStatus = Constants.STATUS_SUCCESS;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.message("Device offline.");
        }

        return requestStatus;
    }

    /**
     * Sets calendar
     *
     * @param calendar calendar for sets
     */
    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    /**
     * Sets location for determine time zone
     *
     * @param location location
     */
    public void setLocation(LatLng location) {
        mLocation = location;
    }
}
