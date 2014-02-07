package ru.neverdark.phototools.azimuth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "appdata";
    private final static int DATABASE_VERSION = 1;
    private final static String CREATE_LOCATIONS_QUERY = "create table locations (_id integer primary key autoincrement, location_name text not null, latitude real not null, longitude real not null, last_access integer not null, map_type integer not null, camera_zoom real not null);";
    
    /**
     * @param context
     */
    public DatabaseHelper(Context context) {
       super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATIONS_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        
    }

}
