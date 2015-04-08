/*******************************************************************************
 * Copyright (C) 2014-2015 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ru.neverdark.phototools.azimuth;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.phototools.azimuth.async.AsyncCalculator;
import ru.neverdark.phototools.azimuth.async.AsyncGeoCoder;
import ru.neverdark.phototools.azimuth.db.LocationRecord;
import ru.neverdark.phototools.azimuth.db.LocationsAdapter;
import ru.neverdark.phototools.azimuth.dialogs.ConfirmDialog;
import ru.neverdark.phototools.azimuth.dialogs.DateTimeDialog;
import ru.neverdark.phototools.azimuth.dialogs.MessageDialog;
import ru.neverdark.phototools.azimuth.dialogs.SaveLocationDialog;
import ru.neverdark.phototools.azimuth.dialogs.ZonePickerDialog;
import ru.neverdark.phototools.azimuth.model.MapApi;
import ru.neverdark.phototools.azimuth.model.SunCalculator;
import ru.neverdark.phototools.azimuth.utils.Common;
import ru.neverdark.phototools.azimuth.utils.Constants;
import ru.neverdark.phototools.azimuth.utils.Log;
import ru.neverdark.phototools.azimuth.utils.Settings;

/**
 * Main application activity
 */
public class PluginActivity extends UfoFragmentActivity {
    private static final int RESULT_SETTINGS = 101;
    private ListView mLocationList;
    private Context mContext;
    private MenuItem mMenuItemSearch;
    private TimeZone mTimeZone;
    private Calendar mCalendar;
    private MenuItem mMenuItemTZ;
    private MenuItem mMenuSave;
    private LocationsAdapter mAdapter;
    private LocationRecord mLocationRecord;
    private CardView mMapInfoCard;
    private TextView mSunInfoTv;
    private TextView mSunriseInfoTv;
    private TextView mSunsetInfoTv;
    private View mSunInfo;
    private View mSunriseInfo;
    private View mSunsetInfo;
    private View mSunAltitudeInfo;
    private TextView mSunAltitudeTv;

    @Override
    public void bindObjects() {
        mContext = this;
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(MapApi.getInstance(mContext));
        setDrawerLayout((DrawerLayout) findViewById(R.id.drawer_layout));
        setDrawerToggle(new ActionBarDrawerToggle(this, getDrawerLayout(), R.string.open_drawer, R.string.close_drawer));
        mLocationList = (ListView) findViewById(R.id.location_list);

        mMapInfoCard = (CardView) findViewById(R.id.map_info_card);
        mSunInfoTv = (TextView) findViewById(R.id.map_sun_tv);
        mSunInfo = findViewById(R.id.map_sun);
        mSunriseInfo = findViewById(R.id.map_sunrise);
        mSunsetInfo = findViewById(R.id.map_sunset);
        mSunriseInfoTv = (TextView) findViewById(R.id.map_sunrise_tv);
        mSunsetInfoTv = (TextView) findViewById(R.id.map_sunset_tv);
        mSunAltitudeInfo = findViewById(R.id.map_altitude);
        mSunAltitudeTv = (TextView) findViewById(R.id.map_altitude_tv);
    }

    @Override
    public void onDestroy() {
        Log.enter();
        MapApi.freeInstance();
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.locations, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        LocationRecord record = mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.remove_location:
                showRemoveLocationDialog(record);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void showRemoveLocationDialog(LocationRecord record) {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        String message = String.format(getString(R.string.deleteConfirmationDialog_message), record.getLocationName());
        dialog.setMessage(message);
        dialog.setCallback(new RemoveLocationListener(record));
        dialog.show(getSupportFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    @Override
    public void setListeners() {
        MapApi.getInstance(mContext).setCallback(new MapListener());
        getDrawerLayout().setDrawerListener(getDrawerToggle());
        mLocationList.setOnItemClickListener(new LocationListClickListener());
        registerForContextMenu(mLocationList);
        mMapInfoCard.setOnClickListener(new CardInfoClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.enter();

        getMenuInflater().inflate(R.menu.main, menu);

        mMenuItemSearch = menu.findItem(R.id.item_search);
        mMenuItemTZ = menu.findItem(R.id.item_timeZone);
        mMenuSave = menu.findItem(R.id.item_saveLocation);

        if (Constants.PAID) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(mMenuItemSearch);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            searchView.setQueryHint(getString(R.string.search_hint));
            searchView.setOnQueryTextListener(new QueryTextListener());
        } else {
            MenuItemCompat.setActionView(mMenuItemSearch, null);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.enter();
        mMenuItemTZ.setVisible(!Settings.isInternetTimeZone(mContext));
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getDrawerToggle().isDrawerIndicatorEnabled()) {
                    if (getDrawerLayout().isDrawerOpen(mLocationList)) {
                        getDrawerLayout().closeDrawer(mLocationList);
                    } else {
                        getDrawerLayout().openDrawer(mLocationList);
                    }
                }
                break;
            case R.id.item_search:
                if (!Constants.PAID) {
                    gotoDonate();
                    break;
                }
                break;
            case R.id.item_map_normal:
                MapApi.getInstance(mContext).setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.item_map_hybrid:
                MapApi.getInstance(mContext).setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.item_map_satellite:
                MapApi.getInstance(mContext).setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.item_map_terrain:
                MapApi.getInstance(mContext).setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.item_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivityForResult(settings, RESULT_SETTINGS);
                break;
            case R.id.item_rate:
                showRate();
                break;
            case R.id.item_feedback:
                showFeedback();
                break;
            case R.id.item_dateTime:
                showDateTimeDialog();
                break;
            case R.id.item_timeZone:
                showZonePickerDialog();
                break;
            case R.id.item_saveLocation:
                showSaveLocationDialog();
                break;
        }

        return true;
    }

    private void showSaveLocationDialog() {
        if (Constants.PAID) {
            if (mLocationRecord == null) {
                mLocationRecord = new LocationRecord();
            }

            if (getSupportActionBar().getSubtitle() != null) {
                mLocationRecord.setLocationName(getSupportActionBar().getSubtitle().toString());
            }
            MapApi.getInstance(mContext).mapToLocation(mLocationRecord);
            SaveLocationDialog dialog = SaveLocationDialog.getInstance(mContext, mLocationRecord);
            dialog.setCallback(new SaveLocationListener());
            dialog.show(getSupportFragmentManager(), SaveLocationDialog.DIALOG_ID);
        } else {
            gotoDonate();
        }
    }

    private void showZonePickerDialog() {
        ZonePickerDialog dialog = ZonePickerDialog.getInstance(mContext);
        dialog.setCallback(new ZonePickerListener());
        dialog.show(getSupportFragmentManager(), ZonePickerDialog.DIALOG_TAG);
    }

    private void showDateTimeDialog() {
        DateTimeDialog dialog = DateTimeDialog.getInstance(mContext, mCalendar);
        dialog.setCallback(new DateTimeDialogListener());
        dialog.show(getSupportFragmentManager(), DateTimeDialog.DIALOG_ID);
    }

    private void showFeedback() {
        Intent mailIntent = new Intent(Intent.ACTION_SEND);
        mailIntent.setType("plain/text");
        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.author_email)});
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        startActivity(Intent.createChooser(mailIntent, getString(R.string.chooseEmailApp)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_activity);
        bindObjects();
        setListeners();

        if (Constants.PAID) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mCalendar = Calendar.getInstance();
        mAdapter = new LocationsAdapter(mContext);
        mLocationList.setAdapter(mAdapter);
    }

    private void gotoDonate() {
        Common.openMarketUrl(mContext, Constants.PAID_PACKAGE);
    }

    @Override
    public void onResume() {
        Log.enter();
        super.onResume();
        if (!Constants.PAID) {
            getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        if (Settings.isShowTip(mContext)) {
            Common.showMessage(mContext, R.string.startupHint);
        }

        mAdapter.openDb();
    }

    private void showErrorDialog(String errorText) {
        MessageDialog dialog = MessageDialog.getInstance(mContext);
        dialog.setMessages(getString(R.string.errorDialog_title), errorText);
        dialog.show(getSupportFragmentManager(), MessageDialog.DIALOG_ID);
    }

    @Override
    public void onPause() {
        if (Constants.PAID) {
            MapApi.getInstance(mContext).saveState();
        }

        mAdapter.closeDb();
        super.onPause();
    }

    private void showRate() {
        Common.openMarketUrl(mContext, mContext.getPackageName());
    }

    private void calculate(LatLng location) {
        boolean isInternetTimezone = Settings.isInternetTimeZone(mContext);
        AsyncCalculator asyncCalc = new AsyncCalculator(this, new CalculationResultListener());
        asyncCalc.setIsInternetTimeZone(isInternetTimezone);
        asyncCalc.setTimeZone(mTimeZone);
        asyncCalc.setLocation(location);
        asyncCalc.setCalendar(mCalendar);
        asyncCalc.execute();
    }

    private void recalculate() {
        MapApi api = MapApi.getInstance(mContext);
        if (api.isHaveMarkder()) {
            calculate(api.getMarkerLocation());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                recalculate();
                break;
        }
    }

    private class LocationListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            getDrawerLayout().closeDrawer(mLocationList);
            mLocationRecord = mAdapter.getItem(position);
            mAdapter.updateAccessTime(mLocationRecord);
            MapApi.getInstance(mContext).locationToMap(mLocationRecord);
            calculate(MapApi.getInstance(mContext).getMarkerLocation());
            getSupportActionBar().setSubtitle(mLocationRecord.getLocationName());
            mMenuSave.setVisible(true);
        }
    }

    private class QueryTextListener implements SearchView.OnQueryTextListener {

        private void startSearchProcess(String query) {
            AsyncGeoCoder geoCoder = new AsyncGeoCoder(mContext, query);
            geoCoder.setCallback(new GeoCoderListener());
            geoCoder.execute();
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            MenuItemCompat.collapseActionView(mMenuItemSearch);
            startSearchProcess(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {

            return false;
        }

        private class GeoCoderListener implements AsyncGeoCoder.OnGeoCoderListener {
            @Override
            public void onGetResultFail() {
                showErrorDialog(getString(R.string.error_geoCoderNotAvailable));
            }

            @Override
            public void onGetResultSuccess(LatLng coordinates, String searchString) {
                if (coordinates != null) {
                    mLocationRecord = null;
                    getSupportActionBar().setSubtitle(searchString);
                    mMapInfoCard.setVisibility(View.GONE);
                    MapApi.getInstance(mContext).moveCamera(coordinates);
                } else {
                    String error = String.format(getString(R.string.error_notFound), searchString);
                    showErrorDialog(error);
                }
            }
        }
    }

    private class MapListener implements MapApi.OnMapListener {
        @Override
        public void onMapLongClick(LatLng latLng) {
            calculate(latLng);
            mMenuSave.setVisible(true);
        }
    }

    private class CalculationResultListener implements AsyncCalculator.OnCalculationResultListener {
        @Override
        public void onGetResultFail() {
            showErrorDialog(getString(R.string.error_timeZoneIsNotDefined));
        }

        @Override
        public void onGetResultSuccess(SunCalculator.CalculationResult calculationResult) {
            double altitude = calculationResult.getAltitude();
            double azimuth = calculationResult.getAzimuth();
            double sunsetAzimuth = calculationResult.getSunsetAzimuth();
            double sunriseAzimuth = calculationResult.getSunriseAzimuth();

            MapApi.getInstance(mContext).setAzimuthData(altitude, azimuth, sunsetAzimuth, sunriseAzimuth);
            MapApi.getInstance(mContext).drawAzimuth(calculationResult.getLocation());

            if (altitude < 0) {
                showErrorDialog(getString(R.string.error_noSun));
            }

            if (altitude >= 0 || Settings.isSunsetShow(mContext) || Settings.isSunriseShow(mContext)) {
                mMapInfoCard.setVisibility(View.VISIBLE);
            } else {
                mMapInfoCard.setVisibility(View.GONE);
            }

            if (altitude >= 0) {
                mSunInfo.setVisibility(View.VISIBLE);
                mSunInfoTv.setText(calculationResult.getTime());
            } else {
                mSunInfo.setVisibility(View.GONE);
            }

            if (Settings.isSunsetShow(mContext)) {
                mSunsetInfo.setVisibility(View.VISIBLE);
                mSunsetInfoTv.setText(calculationResult.getSunsetTime());
            } else {
                mSunsetInfo.setVisibility(View.GONE);
            }

            if (Settings.isSunriseShow(mContext)) {
                mSunriseInfo.setVisibility(View.VISIBLE);
                mSunriseInfoTv.setText(calculationResult.getSunriseTime());
            } else {
                mSunriseInfo.setVisibility(View.GONE);
            }

            if (Settings.isAltitudeShow(mContext) && altitude >= 0) {
                mSunAltitudeInfo.setVisibility(View.VISIBLE);
                mSunAltitudeTv.setText(String.format(Locale.US, "%.3f°", calculationResult.getAltitude()));
            } else {
                mSunAltitudeInfo.setVisibility(View.GONE);
            }
        }
    }

    private class DateTimeDialogListener implements DateTimeDialog.OnPositiveClickListener, OnCallback {
        @Override
        public void onPositiveClick(Calendar calendar) {
            mCalendar = calendar;
            recalculate();
        }
    }

    private class ZonePickerListener implements ZonePickerDialog.OnPositiveClickListener, OnCallback {
        @Override
        public void onPositiveClick(TimeZone tz) {
            mTimeZone = tz;
            recalculate();
        }
    }

    private class SaveLocationListener implements SaveLocationDialog.OnPositiveClickListener, OnCallback {
        @Override
        public void onPositiveClick(LocationRecord data) {
            getSupportActionBar().setSubtitle(data.getLocationName());
            if (data.getId() == -1L) {
                mAdapter.addItem(data);
            } else {
                if (mAdapter.isLocationExists(data)) {
                    mAdapter.updateItem(data);
                } else {
                    mAdapter.addItem(data);
                }
            }
        }
    }

    private class RemoveLocationListener implements ConfirmDialog.OnPositiveClickListener, OnCallback {
        private final LocationRecord mRecord;

        public RemoveLocationListener(LocationRecord record) {
            mRecord = record;
        }

        @Override
        public void onPositiveClickHandler() {
            mAdapter.removeItem(mRecord);
        }
    }

    private class CardInfoClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showDateTimeDialog();
        }
    }
}