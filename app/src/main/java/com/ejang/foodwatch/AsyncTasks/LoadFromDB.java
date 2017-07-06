package com.ejang.foodwatch.AsyncTasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.ejang.foodwatch.Activities.BrowseActivity;
import com.ejang.foodwatch.SQLDB.DatabaseContract;
import com.ejang.foodwatch.Utils.InspectionResult;
import com.ejang.foodwatch.Utils.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


// Asynchronous task for loading all restaurant and inspection data from the SQLite DB. This task
// is called when a user has fetched data from the City of Surrey database in the past week.
public class LoadFromDB extends AsyncTask<SQLiteDatabase, Void, Void> {

    private BrowseActivity activity;
    private HashMap<String, ArrayList<InspectionResult>> inspectionDataToReturn;
    private ArrayList<Restaurant> restaurantDataToReturn;

    public LoadFromDB(BrowseActivity activity)
    {
        // Only BrowseActivity will directly call this constructor, so we can access its fields.
        this.activity = activity;
        inspectionDataToReturn = new HashMap<>();
        restaurantDataToReturn = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(SQLiteDatabase... params) {

        while (!activity.dataAndAdapterAvailable.get())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        activity.dataAndAdapterAvailable.set(false);

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

            addInspectionToMap(new InspectionResult(trackingID, date, type, violLump, hazard, numCrit, numNonCrit));
        }
        inspectionCursor.close();
        activity.setInspectionData(inspectionDataToReturn);

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

            if (inspectionDataToReturn.containsKey(trackingID))
            {
                restaurantDataToReturn.add(new Restaurant(resName, address, latitude,
                        longitude, trackingID, inspectionDataToReturn.get(trackingID)));
            }
            else
            {
                restaurantDataToReturn.add(new Restaurant(resName, address, latitude,
                        longitude, trackingID, null));
            }
        }
        cursor.close();

        // Sort the restaurants by distance from user's selected location and return
        Collections.sort(restaurantDataToReturn, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant o1, Restaurant o2) {
                return o1.distanceFromUser.compareTo(o2.distanceFromUser);
            }
        });

        activity.setRestaurantData(restaurantDataToReturn);

        return null;
    }

    // Called when background task finishes.
    @Override
    protected void onPostExecute(Void v)
    {
        System.err.println("Calling initialize list view after load from db");
        if (!activity.updateCheckerStarted)
        {
            activity.startUpdateChecker();
        }
        activity.initializeListView();
    }

    private void addInspectionToMap(InspectionResult inspection)
    {
        // If trackingID key exists, add it. If not, create new key.
        if (inspectionDataToReturn.containsKey(inspection.trackingID))
        {
            ArrayList<InspectionResult> inspections = inspectionDataToReturn.get(inspection.trackingID);
            Boolean addedInspection = false;
            Integer initialSize = inspections.size();
            // This loop ensures that the inspections for the same key (trackingID) are organized by the date. Most recent inspection is last in the array.
            for (int i = 0 ; i < initialSize ; i++)
            {
                if(inspections.get(i).inspectionDate.after(inspection.inspectionDate))
                {
                    inspections.add(i, inspection);
                    addedInspection = true;
                    break;
                }
            }
            // Covers the case where the inspection to add is the most recent.
            if (!addedInspection)
            {
                inspections.add(inspection);
            }
        }
        else
        {
            ArrayList<InspectionResult> inspectionResults = new ArrayList<>();
            inspectionResults.add(inspection);
            inspectionDataToReturn.put(inspection.trackingID, inspectionResults);
        }
    }
}
