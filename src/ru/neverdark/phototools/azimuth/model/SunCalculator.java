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
package ru.neverdark.phototools.azimuth.model;

import java.util.Calendar;
import java.util.Locale;

import ru.neverdark.phototools.azimuth.utils.Log;

import com.google.android.gms.maps.model.LatLng;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

/**
 * Class for calculation sun azimuth and sun altitude
 */
public class SunCalculator {
    
    /**
     * Class contains calculation result
     */
    public class CalculationResult {
        private double azimuth;
        private double altitude;
        private double sunsetAzimuth;
        private double sunriseAzimuth;

        /**
         * Gets sun altitude
         * 
         * @return sun altitude
         */
        public double getAltitude() {
            return altitude;
        }

        /**
         * Gets sun azimuth in radiant
         * 
         * @return sun azimuth
         */
        public double getAzimuth() {
            return azimuth;
        }

        /**
         * Gets sun azimuth in degrees
         * 
         * @return sun azimuth
         */
        public double getAzimuthInDegrees() {
            return azimuth * 180 / Math.PI;
        }
        
        /**
         * Gets azimuth for sunset time
         * @return sun azimuth for sunset time
         */
        public double getSunsetAzimuth() {
            return sunsetAzimuth;
        }
        
        /**
         * Gets azimuth for sunrise time
         * @return sun azimuth for sunrise time
         */
        public double getSunriseAzimuth() {
            return sunriseAzimuth;
        }
    }

    private class DecRa {
        private double dec;
        private double ra;
    }

    private static final double rad = Math.PI / 180;
    private static final long dayMs = 1000 * 60 * 60 * 24;
    private static final int J1970 = 2440588;

    private static final int J2000 = 2451545;

    private static final double e = rad * 23.4397;

    /**
     * Gets second point for drawing azimuth
     * 
     * @param location
     *            selected location
     * @param azimuth
     *            sun azimuth
     * @param distance
     *            distance to second point (in degrees)
     * @return
     */
    public static LatLng getDestLatLng(LatLng location, double azimuth,
            double distance) {
        double lat2 = location.latitude + distance * Math.cos(azimuth);
        double lng2 = location.longitude + distance * Math.sin(azimuth);

        return new LatLng(lat2, lng2);
    }

    private double asin(double d) {
        return Math.asin(d);
    }

    private double atan(double x, double y) {
        return Math.atan2(x, y);
    }

    private double cos(double d) {
        return Math.cos(d);
    }

    private double getAltitude(double H, double phi, double dec) {
        return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H));
    }

    private double getAzimuth(double H, double phi, double dec) {
        return atan(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi));
    }

    private double getDeclination(double l, double b) {
        return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(l));
    }

    private double getEclipticLongitude(double M, double C) {
        double P = rad * 102.9372;
        return M + C + P + Math.PI;
    }

    private double getEquationOfCenter(double M) {
        return rad
                * (1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003 * sin(3 * M));
    }

    
    /**
     * Gets sun azimuth and altitude for specified date and location
     * 
     * @param date
     *            date for calculation
     * @param location
     *            location for calculation
     * @return object contains calculation result
     */
    public CalculationResult getPosition(Calendar date, LatLng location) {
        long start = Log.enter();

        Log.variable("rad", String.valueOf(rad));
        Log.variable("latitude", String.valueOf(location.latitude));
        Log.variable("longitude", String.valueOf(location.longitude));

        double lw = rad * -location.longitude;
        double phi = rad * location.latitude;

        Log.variable("lw", String.valueOf(lw));
        Log.variable("phi", String.valueOf(phi));

        ////
        double d = toDays(date);
        Log.variable("d", String.valueOf(d));

        DecRa c = getSunCoords(d);
        Log.variable("c.ra", String.valueOf(c.ra));
        Log.variable("c.dec", String.valueOf(c.dec));

        double H = getSiderealTime(d, lw) - c.ra;
        Log.variable("H", String.valueOf(H));
        /////
        
        CalculationResult result = new CalculationResult();
        result.azimuth = getAzimuth(H, phi, c.dec) + Math.PI;
        
        //////////
        // workaround for fix difference between azimuth and photo tools sunset module
        Location loc = new Location(location.latitude, location.longitude);
        SunriseSunsetCalculator calc = new SunriseSunsetCalculator(loc, date.getTimeZone());
        String sunset = calc.getOfficialSunsetForDate(date);
        String sunrise = calc.getOfficialSunriseForDate(date);
        String givenDate = String.format(Locale.US, "%02d%02d", date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
        String[] sunsets = sunset.split(":");
        String[] sunrises = sunrise.split(":");
        sunset = sunsets[0].concat(sunsets[1]);
        sunrise= sunrises[0].concat(sunrises[1]);
        int sunsetInt = Integer.valueOf(sunset);
        int sunriseInt = Integer.valueOf(sunrise);
        int givenDateInt = Integer.valueOf(givenDate);
        Log.variable("sunsetInt", sunset);
        Log.variable("sunriseInt", sunrise);
        Log.variable("givenDateInt", givenDate);
        
        if (givenDateInt >= sunriseInt && givenDateInt <= sunsetInt) {
            result.altitude = 1;
        } else {
            result.altitude = -1;
        }
        
        ////////////
        int sunsetHour = Integer.valueOf(sunsets[0]);
        int sunsetMinute = Integer.valueOf(sunsets[1]);
        int sunriseHour = Integer.valueOf(sunrises[0]);
        int sunriseMinute = Integer.valueOf(sunrises[1]);
        
        Calendar sunsetCalendar = Calendar.getInstance(date.getTimeZone());
        sunsetCalendar.set(Calendar.HOUR_OF_DAY, sunsetHour);
        sunsetCalendar.set(Calendar.MINUTE, sunsetMinute);
        double dSunset = toDays(sunsetCalendar);
        DecRa cSunset = getSunCoords(dSunset);
        double HSunset = getSiderealTime(dSunset, lw) - cSunset.ra;
        result.sunsetAzimuth = getAzimuth(HSunset, phi, cSunset.dec) + Math.PI;
        
        Calendar sunriseCalendar = Calendar.getInstance(date.getTimeZone());
        sunriseCalendar.set(Calendar.HOUR_OF_DAY, sunriseHour);
        sunriseCalendar.set(Calendar.MINUTE, sunriseMinute);
        double dSunrise = toDays(sunriseCalendar);
        DecRa cSunrise = getSunCoords(dSunrise);
        double HSunrise = getSiderealTime(dSunrise, lw) - cSunrise.ra;
        result.sunriseAzimuth = getAzimuth(HSunrise, phi, cSunrise.dec) + Math.PI;
        ////////////
        
        //result.altitude = getAltitude(H, phi, c.dec);

        Log.variable("azimuth", String.valueOf(result.azimuth));
        Log.variable("sunsetAzimuth", String.valueOf(result.sunsetAzimuth));
        Log.variable("sunriseAzimuth", String.valueOf(result.sunriseAzimuth));
        Log.variable("altitude", String.valueOf(result.altitude));

        Log.exit(start);

        return result;
    }

    private double getRightAscension(double l, double b) {
        return atan(sin(l) * cos(e) - tan(b) * sin(e), cos(l));
    }

    private double getSiderealTime(double d, double lw) {
        return rad * (280.16 + 360.9856235 * d) - lw;
    }

    private double getSolarMeanAnomaly(double d) {
        return rad * (357.5291 + 0.98560028 * d);
    }

    private DecRa getSunCoords(double d) {
        DecRa decRa = new DecRa();
        double M = getSolarMeanAnomaly(d);
        double C = getEquationOfCenter(M);
        double L = getEclipticLongitude(M, C);

        decRa.dec = getDeclination(L, 0);
        decRa.ra = getRightAscension(L, 0);

        return decRa;
    }

    private double sin(double d) {
        return Math.sin(d);
    }

    private double tan(double d) {
        return Math.tan(d);
    }

    private double toDays(Calendar date) {
        return toJulian(date) - J2000;
    }

    private double toJulian(Calendar date) {
        Log.enter();
        double first = (double) date.getTimeInMillis() / dayMs;
        double second = J1970 - 0.5;
        Log.variable("first", String.valueOf(first));
        Log.variable("second", String.valueOf(second));
        return first + second;
    }

}
