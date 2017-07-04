package com.ejang.restaurantwatch.AsyncTasks;

/**
 * Created by Eric on 2017-03-26.
 */

import android.content.ContentValues;
import android.os.AsyncTask;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ejang.restaurantwatch.Activities.BrowseActivity;
import com.ejang.restaurantwatch.Utils.InspectionResult;
import com.ejang.restaurantwatch.R;
import com.ejang.restaurantwatch.Utils.Restaurant;
import com.ejang.restaurantwatch.Views.RestaurantListAdapter;
import com.ejang.restaurantwatch.SQLDB.DatabaseContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

// Asynchronous task for downloading and processing JSON object from the City of Surrey web API.
public class DownloadFromWeb extends AsyncTask<JSONObject, String, RestaurantListAdapter>
{
    private BrowseActivity activity;
    private Boolean updateQuietly;
    private HashMap<String, ArrayList<InspectionResult>> inspectionDataToReturn;
    private ArrayList<Restaurant> restaurantDataToReturn;

    public DownloadFromWeb(BrowseActivity activity, Boolean updateQuietly)
    {
        // Only BrowseActivity will directly call this constructor, so we can access its fields.
        this.activity = activity;
        this.updateQuietly = updateQuietly;
        inspectionDataToReturn = new HashMap<>();
        restaurantDataToReturn = new ArrayList<>();
    }

    @Override
    protected RestaurantListAdapter doInBackground(JSONObject... params) {

        System.err.println("update quietly: " + updateQuietly.toString() + "  available: " + String.valueOf(activity.dataAndAdapterAvailable.get()));
        while (!activity.dataAndAdapterAvailable.get())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        activity.dataAndAdapterAvailable.set(false);

        if (updateQuietly){
            System.err.println("CLEARING TABLES AND UDPATING QUIETLY");
//            activity.writeableDB.execSQL(SQL_CLEAR_RES_TABLE);
//            activity.writeableDB.execSQL(SQL_CLEAR_INSPECTION_TABLE);
        }

        // Organize all of the data for setting up the adapter and listview.
        JSONObject response = params[0];
        try
        {
            JSONArray records = response.getJSONObject("result").getJSONArray("records");
            for (int i = 0 ; i < records.length() ; i++)
            {
                JSONObject record = records.getJSONObject(i);
                String date = record.getString("InspectionDate");
                String type = record.getString("InspType");
                String violation = record.getString("ViolLump");
                String hazardRating = record.getString("HazardRating");
                Integer numCrit = record.getInt("NumCritical");
                Integer numNonCrit = record.getInt("NumNonCritical");
                String trackingID = record.getString("TrackingNumber");
                InspectionResult result = new InspectionResult(trackingID, date, type, violation,
                        hazardRating, numCrit, numNonCrit);

                addInspectionToDB(result);
                addInspectionToMap(result);
            }

            // activity.writeableDB.execSQL(SQL_JOIN_TEMP_INSPECTION_TABLE_TO_REAL);
            activity.setInspectionData(inspectionDataToReturn);


            System.err.println("Finished first loop at: " + System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        while (!activity.locationSet.get())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(RestaurantListAdapter adapter) {
        addAllRestaurants();
    }

    private void addInspectionToDB(InspectionResult inspection)
    {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.InspectionTable.COLUMN_TRACKING_ID, inspection.trackingID);
        values.put(DatabaseContract.InspectionTable.COLUMN_INSPECTION_DATE, inspection.inspectionDateString);
        values.put(DatabaseContract.InspectionTable.COLUMN_INSPECTION_TYPE, inspection.inspectionType);
        values.put(DatabaseContract.InspectionTable.COLUMN_INSPECTION_VIOLLUMP, inspection.violLump);
        values.put(DatabaseContract.InspectionTable.COLUMN_INSPECTION_HAZARD, inspection.hazardRatingString);
        values.put(DatabaseContract.InspectionTable.COLUMN_INSPECTION_NUM_CRIT, inspection.numCritical);
        values.put(DatabaseContract.InspectionTable.COLUMN_INSPECTION_NUM_NONCRIT, inspection.numNonCritical);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = activity.writeableDB.insert(DatabaseContract.InspectionTable.INSPECTION_TABLE_NAME, null, values);
    }

    public void addAllRestaurants() {
        // Set up request to get all inspection data. On response, set the adapter and listview
        // in this UI thread. This isn't heavy-weight enough to need an async task currently.
        String url = activity.getString(R.string.url_all_restaurants);
        RequestQueue queue = Volley.newRequestQueue(activity);

        // This method is only called when a location is set, so make the "no location selected" text
        // invisible.
        activity.findViewById(R.id.no_location_selected_text).setVisibility(View.GONE);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.err.println("Got second response at: " + System.currentTimeMillis());
                        try {
                            JSONArray restaurants = response.getJSONObject("result").getJSONArray("records");

                            for (int i = 0; i < restaurants.length(); i++) {
                                JSONObject restaurant = restaurants.getJSONObject(i);
                                String name = restaurant.getString("NAME");
                                String addr = restaurant.getString("PHYSICALADDRESS");
                                Double latitude = restaurant.getDouble("LATITUDE");
                                Double longitude = restaurant.getDouble("LONGITUDE");
                                String trackingID = restaurant.getString("TRACKINGNUMBER");

                                Restaurant restaurantEntry;
                                if (activity.inspectionData.containsKey(trackingID))
                                {
                                    restaurantEntry = new Restaurant(name, addr, latitude,
                                            longitude, trackingID, activity.inspectionData.get(trackingID));
                                }
                                else
                                {
                                    restaurantEntry = new Restaurant(name, addr, latitude,
                                            longitude, trackingID, null);
                                }
                                restaurantDataToReturn.add(restaurantEntry);
                                addRestaurantToDB(restaurantEntry);
                            }
                            System.err.println("Finished second loop at: " + System.currentTimeMillis());
                            Collections.sort(restaurantDataToReturn, new Comparator<Restaurant>() {
                                @Override
                                public int compare(Restaurant o1, Restaurant o2) {
                                    return o1.distanceFromUser.compareTo(o2.distanceFromUser);
                                }
                            });

                            // Once all inspection and restaurant data has been processed, save current
                            // time to shared pref as the refresh time.
                            BrowseActivity.getSharedPref().edit().putLong(activity.getString(R.string.last_refresh_time), System.currentTimeMillis()).commit();

                            activity.setRestaurantData(restaurantDataToReturn);
                            if (!updateQuietly)
                            {
                                activity.initializeListView();
                            }
                            else
                            {
                                activity.askToUpdateAdapter();
                            }
                            if (!activity.updateCheckerStarted)
                            {
                                activity.startUpdateChecker();
                            }

                        } catch (JSONException e) {
                            System.err.println("CAUGHT AN EXCEPTION: ");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("That didn't work! here is the stacktrace: ");
                error.printStackTrace();

            }
        });

        queue.add(jsonRequest);
        System.err.println("Made second http request at: " + System.currentTimeMillis());
    }

    private void addRestaurantToDB(Restaurant restaurant)
    {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RestaurantTable.COLUMN_RES_NAME, restaurant.name);
        values.put(DatabaseContract.RestaurantTable.COLUMN_TRACKING_ID, restaurant.trackingID);
        values.put(DatabaseContract.RestaurantTable.COLUMN_RES_ADDRESS, restaurant.address);
        values.put(DatabaseContract.RestaurantTable.COLUMN_RES_LAT, restaurant.latitude);
        values.put(DatabaseContract.RestaurantTable.COLUMN_RES_LONG, restaurant.longitude);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = activity.writeableDB.insert(DatabaseContract.RestaurantTable.RES_TABLE_NAME, null, values);
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
