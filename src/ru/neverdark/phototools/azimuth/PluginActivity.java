package ru.neverdark.phototools.azimuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import ru.neverdark.phototools.azimuth.model.SunCalculator;
import ru.neverdark.phototools.azimuth.utils.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.widget.Toast;

public class PluginActivity extends SherlockFragmentActivity implements OnMapLongClickListener {

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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
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
        date.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        LatLng latLng = new LatLng(43.121051, 131.890029);
        
        SimpleDateFormat frm = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");
        Log.variable("date", frm.format(date.getTime()));
        
        SunCalculator sunCalc = new SunCalculator();
        SunCalculator.CalculationResult result = sunCalc.getPosition(date, latLng);
        
        
        Toast.makeText(this, String.valueOf(result.getAzimuthInDegres()), Toast.LENGTH_LONG).show();
    }

    /**
     * Sets marker to the long tap position If marker already exists - remove
     * old marker and set new marker in new position
     * @param location location for setting marker
     */
    private void setMarket(LatLng location) {
        // erase old marker if exist
        if (mMarker != null) {
            mMap.clear();
        }
        
        // set new marker
        mMarker = mMap.addMarker(new MarkerOptions().position(location));
    }

}
