package com.ejang.foodwatch.SQLDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        public static final String DATABASE_NAME = "FoodWatch.db";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.myContext = context;
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

        // This is the path where the main DB for the app will be.
        private static String DB_PATH = "/data/data/com.ejang.foodwatch/databases/";
        private static String DB_NAME = "FoodWatch.db";
        private final Context myContext;

        // Creates an empty database file where the main DB will be. Copies contents from default
        // DB located in the assets folder. Throws an exception if it fails to create/copy.
        public void createDefaultDB() throws IOException {

            boolean dbExist = checkDataBase();

            if(!dbExist)
            {
                // This method will create an empty database in the default system path that I can
                // copy the DB asset into.
                this.getReadableDatabase();
                copyDataBase();
            }

        }


        // Check if the database already exist to avoid re-copying the file each time the
        // application is started
        private boolean checkDataBase(){

            SQLiteDatabase checkDB = null;

            try
            {
                String myPath = DB_PATH + DB_NAME;
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

            }
            catch(SQLiteException e)
            {
                //database does't exist yet.
            }

            if(checkDB != null){

                checkDB.close();

            }
            return checkDB != null;
        }

        // Copies database from asset folder to the system folder. This is much faster than using
        // the City of Surrey API.
        private void copyDataBase() throws IOException{

            // Open local DB as the input stream
            InputStream myInput = myContext.getAssets().open(DB_NAME);

            // Path to empty system DB
            String outFileName = DB_PATH + DB_NAME;

            // Open the empty DB as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0)
            {
                System.err.println("COPYING DB");
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }

}
