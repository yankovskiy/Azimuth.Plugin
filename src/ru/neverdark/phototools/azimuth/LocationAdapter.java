package ru.neverdark.phototools.azimuth;

import java.util.ArrayList;
import java.util.List;

import ru.neverdark.phototools.azimuth.utils.Log;

import android.content.Context;
import android.database.SQLException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter linking UI and database
 */
public class LocationAdapter extends ArrayAdapter<LocationRecord> {
    /**
     * The interface for processing clicking on the delete button in the list
     */
    public interface OnRemoveClickListener {
        /**
         * Handler for processing clicking on the delete button in the list
         * @param position position clicking record in the list 
         */
        public void onRemoveClickHandler(final int position);
    }

    /**
     * Holder contains one row from the list
     */
    private static class RowHolder {
        private TextView mLocationName;
        private ImageView mLocationRemoveButton;
    }

    private static final String EXCEPTION_MESSAGE = "Database is not open";

    private OnRemoveClickListener mCallback;
    private List<LocationRecord> mObjects;
    private final int mResource;
    private final Context mContext;

    private LocationsDbAdapter mDbAdapter;

    /**
     * Constructor
     * 
     * @param context
     *            application context
     * @param resource
     *            resource id contains layout for one list record
     */
    public LocationAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<LocationRecord>());
    }

    /**
     * Constructor
     * 
     * @param context
     *            application context
     * @param resource
     *            resource id contains layout for one list record
     * @param objects
     *            the list of objects
     */
    private LocationAdapter(Context context, int resource,
            List<LocationRecord> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
    }

    /**
     * Closes the database connection
     */
    public void closeDb() {
        Log.enter();
        if (mDbAdapter != null) {
            mDbAdapter.close();
            mDbAdapter = null;
        }
    }

    /**
     * Adds a new location in the database
     * 
     * @param locationName
     *            location name
     * @param latitude
     *            location latitude
     * @param longitude
     *            location longitude
     * @param mapType
     *            type of map
     * @param cameraZoom
     *            camera zoom
     * @return added record id
     */
    public long createLocation(String locationName, double latitude,
            double longitude, int mapType, float cameraZoom) {
        Log.enter();
        long id = 0;
        if (mDbAdapter.isOpen()) {
            id = mDbAdapter.createLocation(locationName, latitude, longitude,
                    mapType, cameraZoom);
            mDbAdapter.fetchAllLocations(mObjects);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        notifyDataSetChanged();
        return id;
    }

    /**
     * Deletes the selected record from location list and database
     * 
     * @param record
     *            object containing the record to be deleted
     * 
     * @return true in the case of successful removal of records
     */
    public boolean deleteLocation(LocationRecord record) {
        Log.enter();
        long recordId = record.getId();
        boolean deleteStatus = false;

        if (mDbAdapter.isOpen()) {
            remove(record);
            deleteStatus = mDbAdapter.deleteLocation(recordId);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        notifyDataSetChanged();

        return deleteStatus;
    }

    /**
     * Gets Id record in the database on the position of the element in the list
     * 
     * @param position
     *            position of the element in the list
     * @return id Id record in the databas
     */
    public long getIdByPosition(final int position) {
        Log.enter();
        LocationRecord record = getItem(position);
        return record.getId();
    }

    /* (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.enter();
        View row = convertView;
        RowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
            holder = new RowHolder();
            holder.mLocationRemoveButton = (ImageView) row
                    .findViewById(R.id.locationRow_image_remove);
            holder.mLocationName = (TextView) row
                    .findViewById(R.id.locationRow_label);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        LocationRecord record = mObjects.get(position);
        holder.mLocationName.setText(record.getLocationName());

        setRemoveClickListener(holder, position);

        return row;
    }

    /**
     * Verifies the existence of the location with the specified name
     * 
     * @param locationName
     *            location name for verifies
     * @return true if location with the specified name exists
     */
    public boolean isLocationExists(String locationName) {
        Log.enter();
        boolean exist = false;
        if (mDbAdapter.isOpen()) {
            exist = mDbAdapter.isLocationExists(locationName);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        return exist;
    }

    /**
     * Loads data from a database
     */
    public void loadData() {
        Log.enter();
        if (mDbAdapter.isOpen()) {
            mDbAdapter.fetchAllLocations(mObjects);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }
        notifyDataSetChanged();
    }

    /**
     * Opens a database connection
     */
    public void openDb() {
        Log.enter();
        mDbAdapter = new LocationsDbAdapter(mContext);
        mDbAdapter.open();
    }

    /**
     * Sets callback object object that processing clicking on the delete button in the list
     * 
     * @param callback
     *            object that processing clicking on the delete button in the list
     */
    public void setCallback(OnRemoveClickListener callback) {
        mCallback = callback;
    }

    /**
     * Sets the click handler for "delete" button
     * 
     * @param holder row from the list
     *            
     */
    private void setRemoveClickListener(RowHolder holder, final int position) {
        holder.mLocationRemoveButton
                .setOnClickListener(new View.OnClickListener() {

                    /* (non-Javadoc)
                     * @see android.view.View.OnClickListener#onClick(android.view.View)
                     */
                    @Override
                    public void onClick(View v) {
                        try {
                            mCallback.onRemoveClickHandler(position);
                        } catch (NullPointerException e) {
                            Log.message("Callback not seted");
                        }
                    }

                });
    }

    /**
     * Updates the access time to record. Move new record to a top of the list
     * 
     * @param position
     *            position of the selected item in the list
     */
    public void updateLastAccessTime(final int position) {
        Log.enter();
        long recordId = getIdByPosition(position);

        if (mDbAdapter.isOpen()) {
            mDbAdapter.udateLastAccessTime(recordId);
            mDbAdapter.fetchAllLocations(mObjects);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        notifyDataSetChanged();
    }

    /**
     * Changes saved location in the database
     * 
     * @param recordId
     *            record id in the database
     * @param locationName
     *            location name
     * @param latitude
     *            location latitude
     * @param longitude
     *            location longitude
     * @param mapType
     *            type of map
     * @param cameraZoom
     *            camera zoom
     */
    public void updateLocation(final long recordId, String locationName,
            double latitude, double longitude, int mapType, float cameraZoom) {
        Log.enter();
        if (mDbAdapter.isOpen()) {
            mDbAdapter.updateLocation(recordId, locationName, latitude,
                    longitude, mapType, cameraZoom);

            mDbAdapter.fetchAllLocations(mObjects);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        notifyDataSetChanged();
    }
}
