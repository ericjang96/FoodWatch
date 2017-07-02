package com.ejang.restaurantwatch.AsyncTasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.ejang.restaurantwatch.Activities.BrowseActivity;
import com.ejang.restaurantwatch.Utils.InspectionResult;
import com.ejang.restaurantwatch.Utils.Restaurant;
import com.ejang.restaurantwatch.SQLDB.DatabaseContract;

import java.util.Collections;
import java.util.Comparator;


// Asynchronous task for loading all restaurant and inspection data from the SQLite DB. This task
// is called when a user has fetched data from the City of Surrey database in the past week.
public class LoadFromDB extends AsyncTask<SQLiteDatabase, Void, Void> {

    private BrowseActivity activity;

    public LoadFromDB(BrowseActivity activity)
    {
        // Only BrowseActivity will directly call this constructor, so we can access its fields.
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(SQLiteDatabase... params) {

        SQLiteDatabase writeableDB = params[0];

        // Projection set up for better readability. Also will come in handy if I don't need to get
        // every column from the table in the future.
        String[] projectionRestaurant =
                {
                        DatabaseContract.RestaurantTable.COLUMN_TRACKING_ID,
                        DatabaseContract.RestaurantTable.COLUMN_RES_NAME,
                        DatabaseContract.RestaurantTable.COLUMN_RES_ADDRESS,
                        DatabaseContract.RestaurantTable.COLUMN_RES_LAT,
                        DatabaseContract.RestaurantTable.COLUMN_RES_LONG
                };

        String[] projectionInspection =
                {
                        DatabaseContract.InspectionTable.COLUMN_TRACKING_ID,
                        DatabaseContract.InspectionTable.COLUMN_INSPECTION_DATE,
                        DatabaseContract.InspectionTable.COLUMN_INSPECTION_TYPE,
                        DatabaseContract.InspectionTable.COLUMN_INSPECTION_VIOLLUMP,
                        DatabaseContract.InspectionTable.COLUMN_INSPECTION_HAZARD,
                        DatabaseContract.InspectionTable.COLUMN_INSPECTION_NUM_CRIT,
                        DatabaseContract.InspectionTable.COLUMN_INSPECTION_NUM_NONCRIT
                };

        // Add all inspection data from the database to the global hashmap BrowserActivity.inspectionData
        Cursor inspectionCursor = writeableDB.query(DatabaseContract.InspectionTable.INSPECTION_TABLE_NAME, null, null, null, null, null, null);
        while (inspectionCursor.moveToNext())
        {
            String trackingID = inspectionCursor.getString(inspectionCursor.getColumnIndexOrThrow(projectionInspection[0]));
            String date = inspectionCursor.getString(inspectionCursor.getColumnIndexOrThrow(projectionInspection[1]));
            String type = inspectionCursor.getString(inspectionCursor.getColumnIndexOrThrow(projectionInspection[2]));
            String violLump = inspectionCursor.getString(inspectionCursor.getColumnIndexOrThrow(projectionInspection[3]));
            String hazard = inspectionCursor.getString(inspectionCursor.getColumnIndexOrThrow(projectionInspection[4]));
            Integer numCrit = inspectionCursor.getInt(inspectionCursor.getColumnIndexOrThrow(projectionInspection[5]));
            Integer numNonCrit = inspectionCursor.getInt(inspectionCursor.getColumnIndexOrThrow(projectionInspection[6]));

            activity.addInspectionToMap(new InspectionResult(trackingID, date, type, violLump, hazard, numCrit, numNonCrit));
        }

        // Wait until location is set in the UI thread to initialize restaurant data because they
        // need the distance from the user's chosen location.
        while (!activity.locationSet.get())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Add all restaurant data from the database to the global array BrowserActivity.allRestaurants
        Cursor cursor = writeableDB.query(DatabaseContract.RestaurantTable.RES_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext())
        {
            String trackingID = cursor.getString(cursor.getColumnIndexOrThrow(projectionRestaurant[0]));
            String resName = cursor.getString(cursor.getColumnIndexOrThrow(projectionRestaurant[1]));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(projectionRestaurant[2]));
            Double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(projectionRestaurant[3]));
            Double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(projectionRestaurant[4]));

            if (activity.inspectionData.containsKey(trackingID))
            {
                activity.allRestaurants.add(new Restaurant(resName, address, latitude,
                        longitude, trackingID, activity.inspectionData.get(trackingID)));
            }
            else
            {
                activity.allRestaurants.add(new Restaurant(resName, address, latitude,
                        longitude, trackingID, null));
            }
        }

        // Sort the restaurants by distance from user's selected location and return
        Collections.sort(activity.allRestaurants, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant o1, Restaurant o2) {
                return o1.distanceFromUser.compareTo(o2.distanceFromUser);
            }
        });

        return null;
    }

    // Called when background task finishes.
    @Override
    protected void onPostExecute(Void v) {
        activity.initializeListView();
    }
}
