package ru.neverdark.phototools.azimuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 *
 */
public class LocationsDbAdapter {
    /**
     * 
     */
    public final static String KEY_ROWID = "_id";
    /**
     * 
     */
    public final static String KEY_LOCATION_NAME = "location_name";
    /**
     * 
     */
    public final static String KEY_LATITUDE = "latitude";
    /**
     * 
     */
    public final static String KEY_LONGITUDE = "longitude";
    /**
     * 
     */
    public final static String KEY_LAST_ACCESS = "last_access";
    /**
     * 
     */
    public final static String KEY_MAP_TYPE = "map_type";
    /**
     * 
     */
    public final static String KEY_CAMERA_ZOOM = "camera_zoom";
    /**
     * 
     */
    private final static String TABLE_NAME = "locations";

    private Context mContext;
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    public LocationsDbAdapter(Context context) {
        mContext = context;
    }

    /**
     * 
     */
    public void close() {
        mDbHelper.close();
    }

    /**
     * @param locationName
     * @param latitude
     * @param longitude
     * @param mapType
     * @param cameraZoom
     * @return
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
     * @param locationName
     * @param latitude
     * @param longitude
     * @param mapType
     * @param cameraZoom
     * @return
     */
    public long createLocation(String locationName, double latitude,
            double longitude, int mapType, float cameraZoom) {
        ContentValues values = createContentValues(locationName, latitude,
                longitude, mapType, cameraZoom);
        return mDb.insert(TABLE_NAME, null, values);
    }

    /**
     * @param recordId
     * @return
     */
    public boolean deleteLocation(long recordId) {
        String where = KEY_ROWID.concat(" = ?");
        String[] whereArgs = { String.valueOf(recordId) };
        return mDb.delete(TABLE_NAME, where, whereArgs) > 0;
    }

    /**
     * @return
     */
    public List<LocationRecord> fetchAllLocations() {
        String order = KEY_LAST_ACCESS.concat(" DESC");

        Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null,
                order);

        List<LocationRecord> list = null;
        if (cursor.getCount() > 0) {
            list = new ArrayList<LocationRecord>();
            
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
        
        return list;
    }

    /**
     * @return
     */
    private long getTimeStamp() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        /* Gets desired time as seconds since midnight, January 1, 1970 UTC */
        return calendar.getTimeInMillis() / 1000;
    }

    /**
     * @param locationName
     * @return
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
     * @return
     */
    public boolean isOpen() {
        return mDb.isOpen();
    }

    /**
     * @return
     * @throws SQLException
     */
    public LocationsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * @param recordId
     */
    public void udateLastAccessTime(long recordId) {
        String where = KEY_ROWID.concat(" = ?");
        String[] whereArgs = { String.valueOf(recordId) };
        ContentValues values = new ContentValues();
        values.put(KEY_LAST_ACCESS, getTimeStamp());
        mDb.update(TABLE_NAME, values, where, whereArgs);
    }

    /**
     * @param recordId
     * @param locationName
     * @param latitude
     * @param longitude
     * @param mapType
     * @param cameraZoom
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
