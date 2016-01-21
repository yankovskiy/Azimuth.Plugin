/*******************************************************************************
 * Copyright (C) 2014-2015 Artem Yankovskiy (artemyankovskiy@gmail.com).
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ru.neverdark.phototools.azimuth.model;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.db.LocationRecord;
import ru.neverdark.phototools.azimuth.utils.Common;
import ru.neverdark.phototools.azimuth.utils.Constants;
import ru.neverdark.phototools.azimuth.utils.Settings;

public class MapApi implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener {
    private static final float FIND_ZOOM = 14f;
    private static final float MINIMUM_ZOOM = 5.0f;
    private static MapApi mInstance;
    private float mOldZoom = -1;
    private double mAzimuth;
    private double mSunsetAzimuth;
    private double mSunriseAzimuth;
    private double mAltitude;
    private Polyline mAzimuthLine;
    private Polyline mSunsetAzimuthLine;
    private Polyline mSunriseAzimuthLine;
    private LatLng mLocation;
    private GoogleMap mGoogleMap;
    private Marker mMarker;
    private Context mContext;
    private OnMapListener mCallback;

    public static MapApi getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MapApi();
            mInstance.mContext = context;
        }

        return mInstance;
    }

    public static void freeInstance() {
        if (mInstance != null) {
            mInstance = null;
        }
    }

    public void setCallback(OnMapListener callback) {
        mCallback = callback;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnCameraChangeListener(this);
        if (Constants.PAID) {
            loadState();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        setMarker(latLng);

        if (mCallback != null) {
            mCallback.onMapLongClick(latLng);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mMarker != null) {
            if (mOldZoom != cameraPosition.zoom) {
                mOldZoom = cameraPosition.zoom;
                drawAzimuth(mLocation);
            }
        }
    }

    public void setMapType(int mapType) {
        mGoogleMap.setMapType(mapType);
    }

    public void clearMap() {
        if (mMarker != null) {
            mGoogleMap.clear();
            mMarker = null;
        }
    }

    public void moveCamera(LatLng coordinates) {
        moveCamera(coordinates, FIND_ZOOM);
    }

    public void moveCamera(LatLng coordinates, float zoom) {
        moveCamera(coordinates, zoom, true);
    }

    public void moveCamera(LatLng coordinates, float zoom, boolean isAnimated) {
        clearMap();
        CameraPosition position = new CameraPosition.Builder().target(coordinates).zoom(zoom).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        if (isAnimated) {
            mGoogleMap.animateCamera(update);
        } else {
            mGoogleMap.moveCamera(update);
        }
    }

    public void saveState() {
        Settings.saveMapType(mContext, mGoogleMap.getMapType());
        Settings.saveMapCameraZoom(mContext, getCameraZoom());
        Settings.saveMapCameraPosition(mContext, getCameraPosition());
        Settings.saveMap(mContext);
    }

    public void loadState() {
        if (Settings.isMapSaved(mContext)) {
            LatLng latLng = Settings.getMapCameraPos(mContext);
            float zoom = Settings.getMapCameraZoom(mContext);
            mGoogleMap.setMapType(Settings.getMapType(mContext));
            moveCamera(latLng, zoom, false);
        }
    }

    private void setMarker(LatLng coordinates) {
        clearMap();
        mLocation = coordinates;
        mMarker = mGoogleMap.addMarker(new MarkerOptions().position(coordinates));
    }

    private float getCameraZoom() {
        return mGoogleMap.getCameraPosition().zoom;
    }

    private LatLng getCameraPosition() {
        return mGoogleMap.getCameraPosition().target;
    }

    public void setAzimuthData(double altitude, double azimuth, double sunsetAzimuth, double sunriseAzimuth) {
        mAzimuth = azimuth;
        mSunsetAzimuth = sunsetAzimuth;
        mSunriseAzimuth = sunriseAzimuth;
        mAltitude = altitude;
    }

    public void drawAzimuth(LatLng latLng) {
        if (getCameraZoom() >= MINIMUM_ZOOM) {
            double size = mGoogleMap.getProjection().getVisibleRegion().farLeft.longitude -
                    mGoogleMap.getProjection().getVisibleRegion().nearRight.longitude;
            size = Math.abs(size);

            if (mAzimuthLine != null) {
                mAzimuthLine.remove();
            }

            if (mSunsetAzimuthLine != null) {
                mSunsetAzimuthLine.remove();
            }

            if (mSunriseAzimuthLine != null) {
                mSunriseAzimuthLine.remove();
            }

            if (mAltitude > 0) {
                PolylineOptions options = polylineDraw(
                        latLng,
                        mAzimuth,
                        size,
                        Settings.getSunLineColor(mContext),
                        5
                );
                mAzimuthLine = mGoogleMap.addPolyline(options);
            }

            if (Settings.isSunsetShow(mContext)) {
                PolylineOptions sunsetAzimuth = polylineDraw(
                        latLng,
                        mSunsetAzimuth,
                        size,
                        Settings.getSunsetLineColor(mContext),
                        5
                );
                mSunsetAzimuthLine = mGoogleMap.addPolyline(sunsetAzimuth);
            }

            if (Settings.isSunriseShow(mContext)) {
                PolylineOptions sunriseAzimuth = polylineDraw(
                        latLng,
                        mSunriseAzimuth,
                        size,
                        Settings.getSunriseLineColor(mContext),
                        5
                );

                mSunriseAzimuthLine = mGoogleMap.addPolyline(sunriseAzimuth);
            }
        } else {
            Common.showMessage(mContext, R.string.error_zoomToSmall);
        }
    }

    /**
     * Creates polyline object for drawing azimuth
     *
     * @param location start point
     * @param azimuth  solar azimuth angle
     * @param size     line length for drawing
     * @param color    line color
     * @param width    line width
     * @return polyline object for drawing azimuth
     */
    private PolylineOptions polylineDraw(LatLng location, double azimuth, double size, int color,
                                         int width) {
        PolylineOptions options = new PolylineOptions();
        options.add(location);
        options.add(SunCalculator.getDestLatLng(location, azimuth, size));
        options.width(width);
        options.color(color);

        return options;
    }

    public boolean isHaveMarkder() {
        return mMarker != null;
    }

    public LatLng getMarkerLocation() {
        return mLocation;
    }

    public void locationToMap(LocationRecord data) {
        LatLng location = new LatLng(data.getLatitude(), data.getLongitude());
        setMapType(data.getMapType());
        moveCamera(location, data.getCameraZoom(), true);
        setMarker(location);
    }

    public void mapToLocation(LocationRecord data) {
        data.setMapType(mGoogleMap.getMapType());
        data.setLocation(mLocation);
        data.setCameraZoom(getCameraZoom());
    }


    public interface OnMapListener {
        public void onMapLongClick(LatLng latLng);
    }
}
