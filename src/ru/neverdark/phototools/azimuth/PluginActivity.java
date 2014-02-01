package ru.neverdark.phototools.azimuth;

import java.util.Calendar;
import java.util.TimeZone;

import ru.neverdark.phototools.azimuth.model.SunCalculator;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

public class PluginActivity extends SherlockFragmentActivity implements
        OnMapLongClickListener, OnCameraChangeListener {

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_activity);

        initMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        }

        return true;
    }

    @Override
    public void onMapLongClick(LatLng location) {
        setMarket(location);

        Calendar date = Calendar.getInstance();
        date.set(2014, 01, 31, 14, 0);
        date.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        
        SunCalculator sunCalc = new SunCalculator();
        SunCalculator.CalculationResult result = sunCalc.getPosition(date,
                location);
        
        double size = mMap.getProjection().getVisibleRegion().farLeft.longitude - 
                mMap.getProjection().getVisibleRegion().nearRight.longitude;
        size = Math.abs(size);
        
        // TODO zoom ограничить 4, если меньше трех выдавать ошибку о невозможности расчета
        PolylineOptions options = new PolylineOptions();
        options.add(location);
        options.add(sunCalc.getDestLatLng(location, result.getAzimuth(), size));
        options.width(5);
        options.color(Color.RED);
        
        mMap.addPolyline(options);        
        
        Toast.makeText(this, String.valueOf(result.getAltitude()),
                Toast.LENGTH_LONG).show();
    }

    private double getDistanceByMapZoom() {
        
        float zoom = mMap.getCameraPosition().zoom;
        
        
        return 0;
    }

    /**
     * Sets marker to the long tap position If marker already exists - remove
     * old marker and set new marker in new position
     * 
     * @param location
     *            location for setting marker
     */
    private void setMarket(LatLng location) {
        // erase old marker if exist
        if (mMarker != null) {
            mMap.clear();
        }

        // set new marker
        mMarker = mMap.addMarker(new MarkerOptions().position(location));
    }

    @Override
    public void onCameraChange(CameraPosition camera) {
        // TODO Auto-generated method stub
        
    }

}
