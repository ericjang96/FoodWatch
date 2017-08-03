package com.ejang.foodwatch.Utils;

import com.ejang.foodwatch.Activities.BrowseActivity;
import com.ejang.foodwatch.BuildConfig;

import java.util.ArrayList;

/**
 * Created by Eric on 2017-03-22.
 */

public class Restaurant {

    public String name;
    public String address;
    public Double latitude;
    public Double longitude;
    public String trackingID;
    public ArrayList<InspectionResult> inspectionResults;
    public Float distanceFromUser;
    public HazardRating mostRecentSafety;

    public Restaurant(String name, String address, Double latitude, Double longitude, String trackingID, ArrayList<InspectionResult> inspections)
    {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.trackingID = trackingID;
        this.inspectionResults = inspections;
        updateDistanceFromUser();
        if (inspections == null || inspections.size() == 0)
        {
            mostRecentSafety = HazardRating.UNKNOWN;
        }
        else
        {
            if (BuildConfig.DEBUG)
            {
                // debug builds have the newest inspection at the end of the array.
                mostRecentSafety = inspections.get(inspections.size() - 1).hazardRating;
            }
            else
            {
                mostRecentSafety = inspections.get(0).hazardRating;
            }
        }
    }

    public void updateDistanceFromUser()
    {
        float[] results = {0,0,0};
        android.location.Location.distanceBetween(this.latitude, this.longitude, BrowseActivity.getUserLat(), BrowseActivity.getUserLong(), results);
        this.distanceFromUser = Float.valueOf(results[0]);
    }
}
