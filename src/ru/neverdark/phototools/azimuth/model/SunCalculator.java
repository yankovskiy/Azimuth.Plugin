package ru.neverdark.phototools.azimuth.model;

import java.util.Calendar;

import ru.neverdark.phototools.azimuth.utils.Log;

import com.google.android.gms.maps.model.LatLng;

public class SunCalculator {
    public class CalculationResult {
        private double azimuth;
        private double altitude;

        public double getAltitude() {
            return altitude;
        }

        public double getAzimuth() {
            return azimuth;
        }
        
        public double getAzimuthInDegres() {
            return azimuth * 180 / Math.PI;
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

    private double acos(double d) {
        return Math.acos(d);
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

    private Calendar fromJulian(double j) {
        Calendar calendar = Calendar.getInstance();
        long milliseconds = (long) ((j + 0.5 - J1970) * dayMs);
        calendar.setTimeInMillis(milliseconds);
        return calendar;
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

    public CalculationResult getPosition(Calendar date, LatLng location) {
        long start = Log.enter();

        Log.variable("rad", String.valueOf(rad));
        Log.variable("latitude", String.valueOf(location.latitude));
        Log.variable("longitude", String.valueOf(location.longitude));
        
        double lw = rad * -location.longitude;
        double phi = rad * location.latitude;
        
        Log.variable("lw", String.valueOf(lw));
        Log.variable("phi", String.valueOf(phi));
        
        double d = toDays(date);
        Log.variable("d", String.valueOf(d));

        DecRa c = getSunCoords(d);
        Log.variable("c.ra", String.valueOf(c.ra));
        Log.variable("c.dec", String.valueOf(c.dec));
        
        double H = getSiderealTime(d, lw) - c.ra;
        Log.variable("H", String.valueOf(H));

        CalculationResult result = new CalculationResult();
        result.azimuth = getAzimuth(H, phi, c.dec);
        result.altitude = getAltitude(H, phi, c.dec);
        
        Log.variable("azimuth", String.valueOf(result.azimuth));
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
