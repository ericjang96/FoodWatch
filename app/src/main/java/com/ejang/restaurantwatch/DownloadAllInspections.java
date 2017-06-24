package com.ejang.restaurantwatch;

/**
 * Created by Eric on 2017-03-26.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DownloadAllInspections extends AsyncTask<JSONObject, String, RestaurantListAdapter>
{
    private BrowseActivity activity;

    public DownloadAllInspections(BrowseActivity activity)
    {
        // Only BrowseActivity will directly call this constructor, so we can access its fields.
        this.activity = activity;
    }

    @Override
    protected RestaurantListAdapter doInBackground(JSONObject... params) {
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
                String numCrit = record.getString("NumCritical");
                String numNonCrit = record.getString("NumNonCritical");
                String trackingID = record.getString("TrackingNumber");
                InspectionResult result = new InspectionResult(date, type, violation,
                        hazardRating, numCrit, numNonCrit);

                // If trackingID key exists, add it. If not, create new key.
                if (activity.inspectionData.containsKey(trackingID))
                {
                    ArrayList<InspectionResult> inspections = activity.inspectionData.get(trackingID);
                    Boolean addedInspection = false;
                    Integer initialSize = inspections.size();
                    for (int e = 0 ; e < initialSize ; e++)
                    {
                        if(inspections.get(e).inspectionDate.after(result.inspectionDate))
                        {
                            inspections.add(e, result);
                            addedInspection = true;
                            break;
                        }
                    }
                    if (!addedInspection)
                    {
                        inspections.add(result);
                    }
                }
                else
                {
                    ArrayList<InspectionResult> inspectionResults = new ArrayList<>();
                    inspectionResults.add(result);
                    activity.inspectionData.put(trackingID, inspectionResults);
                }
            }
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
        activity.addAllRestaurants();
    }

}
