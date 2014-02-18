package ru.neverdark.phototools.azimuth;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Class provides API for interaction with database
 */
public class LocationsDbAdapter {
    /**
     * Name of field contains the record id
     */
    public final static String KEY_ROWID = "_id";
    /**
     * Name of field contains the location name
     */
    public final static String KEY_LOCATION_NAME = "location_name";
    /**
     * Name of field contains the location latitude 
     */
    public final static String KEY_LATITUDE = "latitude";
    /**
     * Name of field contains the location longitude
     */
    public final static String KEY_LONGITUDE = "longitude";
    /**
     * Name of field contains the last access time
     */
    public final static String KEY_LAST_ACCESS = "last_access";
    /**
     * Name of field contains the type of map
     */
    public final static String KEY_MAP_TYPE = "map_type";
    /**
     * Name of field contains the camera zoom
     */
    public final static String KEY_CAMERA_ZOOM = "camera_zoom";
    /**
     * Name of the table contains a locations
     */
    private final static String TABLE_NAME = "locations";

    private Context mContext;
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    /**
     * Constructor
     * @param context application context
     */
    public LocationsDbAdapter(Context context) {
        mContext = context;
    }

    /**
     * Closes the database connection
     */
    public void close() {
        mDbHelper.close();
    }

    /**
     * Creates a set of values to add / remove into database
     * @param locationName location name
     * @param latitude location latitude
     * @param longitude location longitude
     * @param mapType type of map
     * @param cameraZoom camera zoom
     * @return set of values
     */
    private ContentValues createContentValues(String locationName,
            double latitude, double longitude, int mapType, float cameraZoom) {
        ContentValues values = new ContentValues();

        values.put(KEY_LOCATION_NAME, locationName);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_LAST_ACCESS, getTimeStamp());
        values.put(KEY_MAP_TYPE, mapType);
        values.put(KEY_CAMERA_ZOOM, cameraZoom);

        return values;
    }

    /**
     * Adds a new location in the database
     * @param locationName location name
     * @param latitude location latitude
     * @param longitude location longitude
     * @param mapType map of type
     * @param cameraZoom camera zoom
     * @return added record id
     */
    public long createLocation(String locationName, double latitude,
            double longitude, int mapType, float cameraZoom) {
        ContentValues values = createContentValues(locationName, latitude,
                longitude, mapType, cameraZoom);
        return mDb.insert(TABLE_NAME, null, values);
    }

    /**
     * Deletes location from database
     * @param recordId record id for delete
     * @return true in the case of successful removal of records
     */
    public boolean deleteLocation(long recordId) {
        String where = KEY_ROWID.concat(" = ?");
        String[] whereArgs = { String.valueOf(recordId) };
        return mDb.delete(TABLE_NAME, where, whereArgs) > 0;
    }

    /**
     * Gets all locations from database
     * @param list list to store data from a database
     */
    public void fetchAllLocations(List<LocationRecord> list) {
        String order = KEY_LAST_ACCESS.concat(" DESC");

        Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null,
                order);

        if (cursor.getCount() > 0) {
            list.clear();

            int idColumn = cursor.getColumnIndex(KEY_ROWID);
            int locationNameColumn = cursor.getColumnIndex(KEY_LOCATION_NAME);
            int latitudeColumn = cursor.getColumnIndex(KEY_LATITUDE);
            int longitudeColumn = cursor.getColumnIndex(KEY_LONGITUDE);
            int lastAccessColumn = cursor.getColumnIndex(KEY_LAST_ACCESS);
            int mapTypeColumn = cursor.getColumnIndex(KEY_MAP_TYPE);
            int cameraZoomColumn = cursor.getColumnIndex(KEY_CAMERA_ZOOM);

            while (cursor.moveToNext()) {
                LocationRecord record = new LocationRecord();
                record.setId(cursor.getLong(idColumn));
                record.setLocationName(cursor.getString(locationNameColumn));
                record.setLatitude(cursor.getDouble(latitudeColumn));
                record.setLongitude(cursor.getDouble(longitudeColumn));
                record.setLastAccess(cursor.getLong(lastAccessColumn));
                record.setMapType(cursor.getInt(mapTypeColumn));
                record.setCameraZoom(cursor.getFloat(cameraZoomColumn));

                list.add(record);
            }
        }
        cursor.close();
    }

    /**
     * Gets current time stamp in Unix epoch
     * @return time stamp
     */
    private long getTimeStamp() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        /* Gets desired time as seconds since midnight, January 1, 1970 UTC */
        return calendar.getTimeInMillis() / 1000;
    }

    /**
     * Verifies the existence of the location with the specified name
     * @param locationName location name for verifies
     * @return true if location with the specified name exists
     */
    public boolean isLocationExists(String locationName) {
        boolean exists = false;
        String where = KEY_LOCATION_NAME.concat(" = ?");
        String[] whereArgs = { locationName };
        String[] columns = { KEY_ROWID };
        Cursor cursor = mDb.query(TABLE_NAME, columns, where, whereArgs, null,
                null, null);

        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        return exists;
    }

    /**
     * Checks database connection is open
     * @return true if database connection is open
     */
    public boolean isOpen() {
        return mDb.isOpen();
    }

    /**
     * Opens database for read/write
     * @return this object
     * @throws SQLException If an error occurs when opening a database
     */
    public LocationsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Updates last access time for record
     * @param recordId record id for update
     */
    public void udateLastAccessTime(long recordId) {
        String where = KEY_ROWID.concat(" = ?");
        String[] whereArgs = { String.valueOf(recordId) };
        ContentValues values = new ContentValues();
        values.put(KEY_LAST_ACCESS, getTimeStamp());
        mDb.update(TABLE_NAME, values, where, whereArgs);
    }

    /**
     * Changes saved location in the database
     * @param recordId record id
     * @param locationName location name
     * @param latitude location latitude
     * @param longitude location longitude
     * @param mapType type of map
     * @param cameraZoom camera zoom
     */
    public void updateLocation(long recordId, String locationName,
            double latitude, double longitude, int mapType, float cameraZoom) {
        String where = KEY_ROWID.concat(" = ?");
        String[] whereArgs = { String.valueOf(recordId) };

        ContentValues values = createContentValues(locationName, latitude,
                longitude, mapType, cameraZoom);
        mDb.update(TABLE_NAME, values, where, whereArgs);
    }
}
