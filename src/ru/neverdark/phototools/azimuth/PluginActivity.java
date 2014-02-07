package ru.neverdark.phototools.azimuth;

import java.util.Calendar;

import ru.neverdark.phototools.azimuth.DateTimeDialog.OnConfirmDateTimeListener;
import ru.neverdark.phototools.azimuth.controller.AsyncCalculator;
import ru.neverdark.phototools.azimuth.controller.AsyncCalculator.OnCalculationResultHandle;
import ru.neverdark.phototools.azimuth.model.SunCalculator;
import ru.neverdark.phototools.azimuth.model.SunCalculator.CalculationResult;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

public class PluginActivity extends SherlockFragmentActivity implements
        OnMapLongClickListener, OnCameraChangeListener,
        OnCalculationResultHandle, OnConfirmDateTimeListener {

    private GoogleMap mMap;
    private Marker mMarker;
    private double mAzimuth;
    private double mAltitude;
    private LatLng mLocation;
    private Calendar mCalendar;
    private double mOldZoom = -1;

    private MenuItem mMenuItemDone;

    private static final String MAP_TYPE = "mapType";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String CAMERA_ZOOM = "zoom";
    private static final String IS_SAVED = "isSaved";

    private void showDateTimeDialog() {
        DateTimeDialog dialog = new DateTimeDialog();
        dialog.setCalendar(mCalendar);
        dialog.setCallBack(this);
        dialog.show(getSupportFragmentManager(), DateTimeDialog.DIALOG_TAG);
    }

    private void showSaveLocationDialog() {
        // TODO Auto-generated method stub

    }

    private void drawAzimuth() {
        setMarket();

        double size = mMap.getProjection().getVisibleRegion().farLeft.longitude
                - mMap.getProjection().getVisibleRegion().nearRight.longitude;
        size = Math.abs(size);

        PolylineOptions options = new PolylineOptions();
        options.add(mLocation);
        options.add(SunCalculator.getDestLatLng(mLocation, mAzimuth, size));
        options.width(5);
        options.color(Color.RED);

        mMap.addPolyline(options);
    }

    /**
     * Inits map
     */
    private void initMap() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        int mapType = prefs.getInt(MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapType(mapType);

        if (prefs.getBoolean(IS_SAVED, false) == true) {
            double latitude = prefs.getFloat(LATITUDE, 0);
            double longitude = prefs.getFloat(LONGITUDE, 0);
            float zoom = prefs.getFloat(CAMERA_ZOOM, 0);
            CameraPosition currentPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude)).zoom(zoom).build();
            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(currentPosition));
        }
    }

    @Override
    public void onCameraChange(CameraPosition camera) {
        if (mMarker != null) {
            if (mOldZoom != camera.zoom) {
                drawAzimuth();
                mOldZoom = camera.zoom;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_activity);

        initMap();
        mCalendar = Calendar.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mMenuItemDone = menu.findItem(R.id.item_confirmSelection);
        return true;
    }

    @Override
    public void onGetResultFail() {
        // TODO: показать диалог выбора временной зоны
        Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetResultSuccess(CalculationResult calculationResult) {
        mAzimuth = calculationResult.getAzimuth();
        mAltitude = calculationResult.getAltitude();
        drawAzimuth();
    }

    @Override
    public void onMapLongClick(LatLng location) {
        mLocation = location;
        calculate();

    }

    private void calculate() {
        AsyncCalculator asyncCalc = new AsyncCalculator(this, this);
        asyncCalc.setLocation(mLocation);
        asyncCalc.setCalendar(mCalendar);
        asyncCalc.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.item_map_normal:
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            break;
        case R.id.item_map_terrain:
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            break;
        case R.id.item_map_hybrid:
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            break;
        case R.id.item_map_satellite:
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            break;
        case R.id.item_confirmSelection:
            showSaveLocationDialog();
            break;
        case R.id.item_dateTime:
            showDateTimeDialog();
            break;
        }

        return true;
    }

    @Override
    public void onStop() {
        super.onPause();
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(MAP_TYPE, mMap.getMapType());
        editor.putFloat(CAMERA_ZOOM, mMap.getCameraPosition().zoom);
        editor.putFloat(LATITUDE,
                (float) mMap.getCameraPosition().target.latitude);
        editor.putFloat(LONGITUDE,
                (float) mMap.getCameraPosition().target.longitude);
        editor.putBoolean(IS_SAVED, true);
        editor.commit();
    }

    /**
     * Sets marker to the long tap position If marker already exists - remove
     * old marker and set new marker in new position
     * 
     * @param location
     *            location for setting marker
     */
    private void setMarket() {
        clearMap();

        // set new marker
        mMarker = mMap.addMarker(new MarkerOptions().position(mLocation));
        mMenuItemDone.setVisible(true);
    }

    private void clearMap() {
        if (mMarker != null) {
            mMap.clear();
        }
    }

    @Override
    public void onConfirmDateTimeHandler(Calendar calendar) {
        mCalendar = calendar;
        
        if (mLocation != null) {
            calculate();
        }
    }

}
