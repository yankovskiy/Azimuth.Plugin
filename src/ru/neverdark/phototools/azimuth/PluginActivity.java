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

import java.util.Calendar;
import java.util.TimeZone;

import ru.neverdark.phototools.azimuth.async.AsyncCalculator;
import ru.neverdark.phototools.azimuth.async.AsyncGeoCoder;
import ru.neverdark.phototools.azimuth.async.AsyncGeoCoder.OnGeoCoderListener;
import ru.neverdark.phototools.azimuth.db.LocationAdapter;
import ru.neverdark.phototools.azimuth.db.LocationRecord;
import ru.neverdark.phototools.azimuth.dialogs.DateTimeDialog;
import ru.neverdark.phototools.azimuth.dialogs.DeleteConfirmationDialog;
import ru.neverdark.phototools.azimuth.dialogs.ErrorDialog;
import ru.neverdark.phototools.azimuth.dialogs.SaveLocationDialog;
import ru.neverdark.phototools.azimuth.dialogs.TimeZoneSelectionDialog;
import ru.neverdark.phototools.azimuth.dialogs.DeleteConfirmationDialog.OnDeleteConfirmationListener;
import ru.neverdark.phototools.azimuth.dialogs.TimeZoneSelectionDialog.OnTimeZoneSelectionListener;
import ru.neverdark.phototools.azimuth.model.SunCalculator;
import ru.neverdark.phototools.azimuth.utils.Constants;
import ru.neverdark.phototools.azimuth.utils.Log;
import ru.neverdark.phototools.azimuth.utils.Settings;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Main application activity
 */
public class PluginActivity extends SherlockFragmentActivity implements
        OnMapLongClickListener, OnCameraChangeListener {

    /**
     * Class implements calculation handling
     */
    private class CalculationResultListener implements
            AsyncCalculator.OnCalculationResultListener {
        /*
         * (non-Javadoc)
         * 
         * @see ru.neverdark.phototools.azimuth.controller.AsyncCalculator.
         * OnCalculationResultListener#onGetResultFail()
         */
        @Override
        public void onGetResultFail() {
            showErrorDialog(R.string.error_timeZoneIsNotDefined);
        }

        /*
         * (non-Javadoc)
         * 
         * @see ru.neverdark.phototools.azimuth.controller.AsyncCalculator.
         * OnCalculationResultListener
         * #onGetResultSuccess(ru.neverdark.phototools
         * .azimuth.model.SunCalculator.CalculationResult)
         */
        @Override
        public void onGetResultSuccess(
                SunCalculator.CalculationResult calculationResult) {
            mAzimuth = calculationResult.getAzimuth();
            mAltitude = calculationResult.getAltitude();

            if (mAltitude < 0) {
                showErrorDialog(R.string.error_noSun);
            }
            drawAzimuth();
        }
    }

    /**
     * Class implements date and time selection handler
     */
    private class ConfirmDateTimeListener implements
            DateTimeDialog.OnConfirmDateTimeListener {
        @Override
        public void onConfirmDateTimeHandler(Calendar calendar) {
            mCalendar = calendar;

            if (mLocation != null) {
                calculate();
            }
        }
    }

    /**
     * Class implements delete record action handler
     */
    private class DeleteConfirmationListener implements
            OnDeleteConfirmationListener {
        /*
         * (non-Javadoc)
         * 
         * @see ru.neverdark.phototools.azimuth.DeleteConfirmationDialog.
         * OnDeleteConfirmationListener
         * #onDeleteConfirmationHandler(ru.neverdark.
         * phototools.azimuth.LocationRecord)
         */
        @Override
        public void onDeleteConfirmationHandler(LocationRecord locationRecord) {
            mAdapter.deleteLocation(locationRecord);
        }
    }

    /**
     * Class for handling results from GeoCoder
     */
    private class GeoCoderListener implements OnGeoCoderListener {
        @Override
        public void onGetResultFail() {
            showErrorDialog(R.string.error_geoCoderNotAvailable);
        }

        @Override
        public void onGetResultSuccess(LatLng coordinates, String searchString) {
            if (coordinates != null) {
                clearMap();
                
                mLocation = coordinates;
                float zoom = mMap.getCameraPosition().zoom;

                // move camera to saved position
                CameraPosition currentPosition = new CameraPosition.Builder()
                        .target(mLocation).zoom(zoom).build();
                mMap.moveCamera(CameraUpdateFactory
                        .newCameraPosition(currentPosition));
            } else {
                String errorMessage = String.format(
                        getString(R.string.error_notFound), searchString);
                showErrorDialog(errorMessage);
            }
        }

    }

    /**
     * Class implements clicks handler for location list
     */
    private class LocationItemClickListener implements
            ListView.OnItemClickListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * android.widget.AdapterView.OnItemClickListener#onItemClick(android
         * .widget.AdapterView, android.view.View, int, long)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            selectItem(position);
        }
    }

    /**
     * Class for handling search query
     */
    private class QueryTextListener implements OnQueryTextListener {
        @Override
        public boolean onQueryTextChange(String newText) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.variable("query", query);
            mMenuItemSearch.collapseActionView();
            initSearchProcess(query);
            
            return true;
        }
    }

    /**
     * Class implements handler for processing clicking on the delete button in
     * the list
     */
    private class RemoveClickListener implements
            LocationAdapter.OnRemoveClickListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * ru.neverdark.phototools.azimuth.LocationAdapter.OnRemoveClickListener
         * #onRemoveClickHandler(int)
         */
        @Override
        public void onRemoveClickHandler(final int position) {
            LocationRecord record = mAdapter.getItem(position);
            DeleteConfirmationDialog dialog = DeleteConfirmationDialog
                    .getInstance(mContext);
            dialog.setLocationRecord(record);
            dialog.setCallback(new DeleteConfirmationListener());
            dialog.show(getSupportFragmentManager(),
                    DeleteConfirmationDialog.DIALOG_TAG);
        }
    }

    /**
     * Class implements handler for processing the saving location information
     * into database
     */
    private class SaveLocationListener implements
            SaveLocationDialog.OnSaveLocationListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * ru.neverdark.phototools.azimuth.SaveLocationDialog.OnSaveLocationListener
         * #
         * onSaveLocationHandler(ru.neverdark.phototools.azimuth.SaveLocationDialog
         * .SaveDialogData)
         */
        @Override
        public void onSaveLocationHandler(SaveLocationDialog.SaveDialogData data) {
            final int actionType = data.getActionType();
            switch (actionType) {
            case SaveLocationDialog.ACTION_TYPE_NEW:
                long id = mAdapter.createLocation(data.getLocationRecord()
                        .getLocationName(), data.getLocationRecord()
                        .getLatitude(),
                        data.getLocationRecord().getLongitude(), data
                                .getLocationRecord().getMapType(), data
                                .getLocationRecord().getCameraZoom());
                mSaveDialogData.getLocationRecord().setId(id);
                mSaveDialogData
                        .setActionType(SaveLocationDialog.ACTION_TYPE_EDIT);
                break;
            case SaveLocationDialog.ACTION_TYPE_EDIT:
                mAdapter.updateLocation(data.getLocationRecord().getId(), data
                        .getLocationRecord().getLocationName(), data
                        .getLocationRecord().getLatitude(), data
                        .getLocationRecord().getLongitude(), data
                        .getLocationRecord().getMapType(), data
                        .getLocationRecord().getCameraZoom());
                break;
            }
            mLocationList.setItemChecked(0, true);
            setTitle(data.getLocationRecord().getLocationName());
        }
    }

    /**
     * Class implements handler for processing time zone selection
     */
    private class TimeZoneSelectionListener implements
            OnTimeZoneSelectionListener {
        /*
         * (non-Javadoc)
         * 
         * @see ru.neverdark.phototools.azimuth.TimeZoneSelectionDialog.
         * OnTimeZoneSelectionListener
         * #onTimeZoneSelectionHandler(java.util.TimeZone)
         */
        @Override
        public void onTimeZoneSelectionHandler(TimeZone timeZone) {
            mTimeZone = timeZone;
            if (mMarker != null) {
                calculate();
            }
        }
    }

    private static final String CAMERA_ZOOM = "zoom";
    private static final String IS_SAVED = "isSaved";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String MAP_TYPE = "mapType";
    private static final float MINIMUM_ZOOM_SIZE = 5.0f;

    private LocationAdapter mAdapter;
    private double mAltitude;
    private double mAzimuth;
    private Calendar mCalendar;
    private Context mContext;
    private DrawerLayout mDrawerLayout;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsMenuItemDoneVisible;
    private LatLng mLocation;
    private ListView mLocationList;
    private GoogleMap mMap;
    private Marker mMarker;
    private MenuItem mMenuItemDateTime;
    private MenuItem mMenuItemDone;
    private MenuItem mMenuItemSearch;
    private MenuItem mMenuItemTimeZone;
    private double mOldZoom = -1;
    private String mPackageName;
    private final SaveLocationDialog.SaveDialogData mSaveDialogData;
    private SearchView mSearchView;
    private TimeZone mTimeZone;
    private CharSequence mTitle;

    /**
     * Constructor
     */
    public PluginActivity() {
        mSaveDialogData = new SaveLocationDialog.SaveDialogData();
        mSaveDialogData.setLocationRecord(new LocationRecord());
        mIsMenuItemDoneVisible = false;
        mTimeZone = null;
    }

    /**
     * Bind class objects to resources
     */
    private void bindObjectToResource() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLocationList = (ListView) findViewById(R.id.location_list);
    }

    /**
     * Initializes thread for azimuth calculation
     */
    private void calculate() {
        boolean isInternetTimezone = Settings.isInternetTimeZone(mContext);
        AsyncCalculator asyncCalc = new AsyncCalculator(this,
                new CalculationResultListener());
        asyncCalc.setIsInternetTimeZone(isInternetTimezone);
        asyncCalc.setTimeZone(mTimeZone);
        asyncCalc.setLocation(mLocation);
        asyncCalc.setCalendar(mCalendar);
        asyncCalc.execute();
    }

    /**
     * Clears map from any objects (marker, azimuth line)
     */
    private void clearMap() {
        if (mMarker != null) {
            mMap.clear();
            mMarker = null;
        }
    }

    /**
     * Draws azimuth on the map
     */
    private void drawAzimuth() {
        setMarker();

        if (mMap.getCameraPosition().zoom >= MINIMUM_ZOOM_SIZE) {
            if (mAltitude > 0) {
                double size = mMap.getProjection().getVisibleRegion().farLeft.longitude
                        - mMap.getProjection().getVisibleRegion().nearRight.longitude;
                size = Math.abs(size);

                PolylineOptions options = new PolylineOptions();
                options.add(mLocation);
                options.add(SunCalculator.getDestLatLng(mLocation, mAzimuth,
                        size));
                options.width(5);
                options.color(Color.RED);

                mMap.addPolyline(options);
            }
        } else {
            showMessage(R.string.error_zoomToSmall);
        }
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

        if (Constants.PAID) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

            int mapType = prefs.getInt(MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMapType(mapType);

            if (prefs.getBoolean(IS_SAVED, false) == true) {
                double latitude = prefs.getFloat(LATITUDE, 0);
                double longitude = prefs.getFloat(LONGITUDE, 0);
                float zoom = prefs.getFloat(CAMERA_ZOOM, 0);
                CameraPosition currentPosition = new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude)).zoom(zoom)
                        .build();
                mMap.moveCamera(CameraUpdateFactory
                        .newCameraPosition(currentPosition));
            }
        }
    }

    /**
     * Inits process for searching coordinates by address
     * 
     * @param query
     *            address for searching in GeoCoder
     */
    public void initSearchProcess(String query) {
        AsyncGeoCoder geo = new AsyncGeoCoder(mContext);
        geo.setSearchString(query);
        geo.setCallback(new GeoCoderListener());
        geo.execute();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.android.gms.maps.GoogleMap.OnCameraChangeListener#onCameraChange
     * (com.google.android.gms.maps.model.CameraPosition)
     */
    @Override
    public void onCameraChange(CameraPosition camera) {
        // if marker exist and we change zoom - redraw azimuth
        if (mMarker != null) {
            if (mOldZoom != camera.zoom) {
                drawAzimuth();
                mOldZoom = camera.zoom;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.actionbarsherlock.app.SherlockFragmentActivity#onConfigurationChanged
     * (android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (Constants.PAID) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_activity);
        bindObjectToResource();

        initMap();

        if (Constants.PAID) {
            setTitle(getString(R.string.app_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            mDrawerTitle = getString(R.string.drawer_title);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer, R.string.open_drawer,
                    R.string.close_drawer) {
                @Override
                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu();
                    getSupportActionBar().setTitle(mTitle);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    supportInvalidateOptionsMenu();
                    getSupportActionBar().setTitle(mDrawerTitle);
                }
            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
        } else {
            setTitle(getString(R.string.app_title_free));
        }

        mCalendar = Calendar.getInstance();
        mContext = this;
        mPackageName = mContext.getPackageName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu
     * (android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mMenuItemDone = menu.findItem(R.id.item_saveLocation);
        mMenuItemDateTime = menu.findItem(R.id.item_dateTime);
        mMenuItemTimeZone = menu.findItem(R.id.item_timeZone);
        mMenuItemSearch = menu.findItem(R.id.item_search);
        
        if (Constants.PAID) {
            mSearchView = new SearchView(mContext);
            mSearchView.setQueryHint(getString(R.string.search_hint));
            mSearchView.setOnQueryTextListener(new QueryTextListener());
            mMenuItemSearch.setActionView(mSearchView);
        } else {
            mMenuItemSearch.setActionView(null);
        }
        
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.android.gms.maps.GoogleMap.OnMapLongClickListener#onMapLongClick
     * (com.google.android.gms.maps.model.LatLng)
     */
    @Override
    public void onMapLongClick(LatLng location) {
        // if marker not present we have new location
        if (mMarker == null) {
            mSaveDialogData.setActionType(SaveLocationDialog.ACTION_TYPE_NEW);
        }

        mLocation = location;
        calculate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected
     * (android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (Constants.PAID) {
                if (mDrawerLayout.isDrawerOpen(mLocationList)) {
                    mDrawerLayout.closeDrawer(mLocationList);
                } else {
                    mDrawerLayout.openDrawer(mLocationList);
                }
            }
            break;
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
        case R.id.item_saveLocation:
            showSaveLocationDialog();
            break;
        case R.id.item_dateTime:
            showDateTimeDialog();
            break;
        case R.id.item_settings:
            showSettings();
            break;
        case R.id.item_timeZone:
            showTimeZoneSelectionDialog();
            break;
        case R.id.item_rate:
            showRate();
            break;
        case R.id.item_feedback:
            showFeedback();
            break;
        case R.id.item_search:
            if (Constants.PAID == false) {
                showErrorDialog(R.string.error_availableOnlyInPaid);
            }
            break;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.actionbarsherlock.app.SherlockFragmentActivity#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

        if (Constants.PAID) {
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
        mAdapter.closeDb();
        mAdapter.clear();
        mAdapter = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.actionbarsherlock.app.SherlockFragmentActivity#onPostCreate(android
     * .os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Constants.PAID) {
            mDrawerToggle.syncState();
        }

        if (Settings.isShowTip(mContext)) {
            showMessage(R.string.startupHint);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.actionbarsherlock.app.SherlockFragmentActivity#onPrepareOptionsMenu
     * (android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        if (Constants.PAID) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLocationList);
            mMenuItemDateTime.setVisible(!drawerOpen);
            mMenuItemDone.setVisible(!drawerOpen && mIsMenuItemDoneVisible);
        } else {
            mMenuItemDone.setVisible(mIsMenuItemDoneVisible);
        }
        mMenuItemTimeZone.setVisible(!Settings.isInternetTimeZone(mContext));
        return super.onPrepareOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    public void onResume() {
        Log.enter();
        super.onResume();
        mAdapter = new LocationAdapter(this, R.layout.location_row);

        mAdapter.setCallback(new RemoveClickListener());
        mAdapter.openDb();
        mAdapter.loadData();

        mLocationList.setAdapter(mAdapter);

        mLocationList.setOnItemClickListener(new LocationItemClickListener());
    }

    /**
     * Selects location from the location list
     * 
     * @param position
     *            location position in the list
     */
    private void selectItem(int position) {
        if (Constants.PAID) {
            LocationRecord record = mAdapter.getItem(position);
            mSaveDialogData.setActionType(SaveLocationDialog.ACTION_TYPE_EDIT);
            mSaveDialogData.setLocationRecord(record);
            mLocation = new LatLng(record.getLatitude(), record.getLongitude());

            // move camera to saved position
            CameraPosition currentPosition = new CameraPosition.Builder()
                    .target(mLocation).zoom(record.getCameraZoom()).build();
            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(currentPosition));

            // sets saved zoom
            mMap.setMapType(record.getMapType());

            mAdapter.updateLastAccessTime(position);
            mLocationList.setItemChecked(0, true);
            mDrawerLayout.closeDrawer(mLocationList);
            setTitle(record.getLocationName());
            // calculate azimuth
            calculate();
        }
    }

    /**
     * Sets marker to the long tap position If marker already exists - remove
     * old marker and set new marker in new position
     * 
     * @param location
     *            location for setting marker
     */
    private void setMarker() {
        clearMap();

        // set new marker
        mMarker = mMap.addMarker(new MarkerOptions().position(mLocation));
        mMenuItemDone.setVisible(true);
        mIsMenuItemDoneVisible = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#setTitle(java.lang.CharSequence)
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * Shows dialog for date and time selection
     */
    private void showDateTimeDialog() {
        DateTimeDialog dialog = DateTimeDialog.getInstance(mContext);
        dialog.setCalendar(mCalendar);
        dialog.setCallBack(new ConfirmDateTimeListener());
        dialog.show(getSupportFragmentManager(), DateTimeDialog.DIALOG_TAG);
    }

    /**
     * Shows error dialog
     * 
     * @param resourceId
     *            resource id contains error message
     */
    private void showErrorDialog(int resourceId) {
        ErrorDialog dialog = ErrorDialog.getIntstance(mContext);
        dialog.setErrorMessage(resourceId);
        dialog.show(getSupportFragmentManager(), ErrorDialog.DIALOG_TAG);
    }

    /**
     * Shows error dialog
     * 
     * @param errorMessage
     *            error message
     */
    private void showErrorDialog(String errorMessage) {
        ErrorDialog dialog = ErrorDialog.getIntstance(mContext);
        dialog.setErrorMessage(errorMessage);
        dialog.show(getSupportFragmentManager(), ErrorDialog.DIALOG_TAG);
    }

    /**
     * Starts activity for sending email to app author
     */
    private void showFeedback() {
        Intent mailIntent = new Intent(Intent.ACTION_SEND);
        mailIntent.setType("plain/text");
        mailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[] { getString(R.string.author_email) });
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        startActivity(Intent.createChooser(mailIntent,
                getString(R.string.chooseEmailApp)));
    }

    /**
     * Shows toast with text
     * 
     * @param resourceId
     *            resource id contains message
     */
    private void showMessage(int resourceId) {
        Toast.makeText(mContext, resourceId, Toast.LENGTH_LONG).show();
    }

    /**
     * Opens Google Play on the page with app
     */
    private void showRate() {
        String url = "market://details?id=".concat(mPackageName);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
        marketIntent.setData(Uri.parse(url));
        startActivity(marketIntent);
    }

    /**
     * Shows dialog for saving location
     */
    private void showSaveLocationDialog() {
        if (Constants.PAID) {
            mSaveDialogData.getLocationRecord().setLatitude(mLocation.latitude);
            mSaveDialogData.getLocationRecord().setLongitude(
                    mLocation.longitude);
            mSaveDialogData.getLocationRecord().setMapType(mMap.getMapType());
            mSaveDialogData.getLocationRecord().setCameraZoom(
                    mMap.getCameraPosition().zoom);

            SaveLocationDialog dialog = SaveLocationDialog
                    .getInstance(mContext);
            dialog.setCallback(new SaveLocationListener());
            dialog.setSaveDialogData(mSaveDialogData);
            dialog.show(getSupportFragmentManager(),
                    SaveLocationDialog.DIALOG_TAG);
        } else {
            showErrorDialog(R.string.error_availableOnlyInPaid);
        }
    }

    /**
     * Shows settings dialog
     */
    private void showSettings() {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    /**
     * Shows dialog for time zone selection
     */
    private void showTimeZoneSelectionDialog() {
        TimeZoneSelectionDialog dialog = TimeZoneSelectionDialog
                .getInstance(mContext);
        dialog.setCallback(new TimeZoneSelectionListener());
        dialog.show(getSupportFragmentManager(),
                TimeZoneSelectionDialog.DIALOG_TAG);
    }

}
