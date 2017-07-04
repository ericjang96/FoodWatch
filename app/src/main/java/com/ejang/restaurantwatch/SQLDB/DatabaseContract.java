package com.ejang.restaurantwatch.SQLDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Eric on 2017-06-24.
 */

public final class DatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static class RestaurantTable implements BaseColumns {
        public static final String RES_TABLE_NAME = "restaurants";
        public static final String COLUMN_TRACKING_ID = "trackingID";
        public static final String COLUMN_RES_NAME = "name";
        public static final String COLUMN_RES_ADDRESS = "address";
        public static final String COLUMN_RES_LAT = "latitute";
        public static final String COLUMN_RES_LONG = "longitude";

    }

    public static class InspectionTable implements BaseColumns
    {
        public static final String INSPECTION_TABLE_NAME = "inspections";
        public static final String COLUMN_TRACKING_ID = "trackingID";
        public static final String COLUMN_INSPECTION_DATE = "date";
        public static final String COLUMN_INSPECTION_TYPE = "type";
        public static final String COLUMN_INSPECTION_VIOLLUMP = "violationlump";
        public static final String COLUMN_INSPECTION_HAZARD = "hazard";
        public static final String COLUMN_INSPECTION_NUM_CRIT = "numcrit";
        public static final String COLUMN_INSPECTION_NUM_NONCRIT = "numnoncrit";
    }

    private static final String SQL_CREATE_RES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + RestaurantTable.RES_TABLE_NAME + " (" +
                    RestaurantTable._ID + " INTEGER PRIMARY KEY," +
                    RestaurantTable.COLUMN_TRACKING_ID + " TEXT," +
                    RestaurantTable.COLUMN_RES_NAME + " TEXT," +
                    RestaurantTable.COLUMN_RES_ADDRESS + " TEXT," +
                    RestaurantTable.COLUMN_RES_LAT + " REAL," +
                    RestaurantTable.COLUMN_RES_LONG + " REAL," +
                    "UNIQUE (" + RestaurantTable.COLUMN_TRACKING_ID + ", " +
                    RestaurantTable.COLUMN_RES_NAME + ", " +
                    RestaurantTable.COLUMN_RES_ADDRESS + ", " +
                    RestaurantTable.COLUMN_RES_LAT + ", " +
                    RestaurantTable.COLUMN_RES_LONG +
                    ") ON CONFLICT REPLACE )";

    public static final String SQL_DELETE_RES_TABLE =
            "DROP TABLE IF EXISTS " + RestaurantTable.RES_TABLE_NAME;

    private static final String SQL_CREATE_INSPECTION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + InspectionTable.INSPECTION_TABLE_NAME + " (" +
                    InspectionTable._ID + " INTEGER PRIMARY KEY," +
                    InspectionTable.COLUMN_TRACKING_ID + " TEXT," +
                    InspectionTable.COLUMN_INSPECTION_DATE + " TEXT," +
                    InspectionTable.COLUMN_INSPECTION_TYPE + " TEXT," +
                    InspectionTable.COLUMN_INSPECTION_VIOLLUMP + " TEXT," +
                    InspectionTable.COLUMN_INSPECTION_HAZARD + " TEXT," +
                    InspectionTable.COLUMN_INSPECTION_NUM_CRIT + " INTEGER," +
                    InspectionTable.COLUMN_INSPECTION_NUM_NONCRIT + " INTEGER, " +
                    "UNIQUE (" + InspectionTable.COLUMN_TRACKING_ID + ", " +
                    InspectionTable.COLUMN_INSPECTION_DATE + ", " +
                    InspectionTable.COLUMN_INSPECTION_TYPE + ", " +
                    InspectionTable.COLUMN_INSPECTION_VIOLLUMP + ", " +
                    InspectionTable.COLUMN_INSPECTION_HAZARD + ", " +
                    InspectionTable.COLUMN_INSPECTION_NUM_CRIT + ", " +
                    InspectionTable.COLUMN_INSPECTION_NUM_NONCRIT +
                     ") ON CONFLICT REPLACE )";

    public static final String SQL_DELETE_INSPECTION_TABLE =
            "DROP TABLE IF EXISTS " + InspectionTable.INSPECTION_TABLE_NAME;

    public static final String SQL_CLEAR_RES_TABLE = "DELETE FROM " + RestaurantTable.RES_TABLE_NAME;

    public static final String SQL_CLEAR_INSPECTION_TABLE = "DELETE FROM " + InspectionTable.INSPECTION_TABLE_NAME;



    public static class DatabaseHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "RestaurantWatch.db";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_RES_TABLE);
            db.execSQL(SQL_CREATE_INSPECTION_TABLE);

        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_RES_TABLE);
            db.execSQL(SQL_DELETE_INSPECTION_TABLE);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

}
