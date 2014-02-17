package ru.neverdark.phototools.azimuth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class takes care of opening the database if it exists, creating it if it does
 * not, and upgrading it as necessary.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "appdata";
    private final static int DATABASE_VERSION = 1;
    private final static String CREATE_LOCATIONS_QUERY = "create table locations (_id integer primary key autoincrement, location_name text not null, latitude real not null, longitude real not null, last_access integer not null, map_type integer not null, camera_zoom real not null);";

    /**
     * Constructor
     * @param context application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATIONS_QUERY);
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

}
