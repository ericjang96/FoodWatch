package com.ejang.restaurantwatch.Utils;

import com.ejang.restaurantwatch.Activities.BrowseActivity;

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

    public Restaurant(String name, String address, Double latitude, Double longitude, String trackingID, ArrayList<InspectionResult> inspections)
    {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.trackingID = trackingID;
        this.inspectionResults = inspections;
        updateDistanceFromUser();
    }

    public void updateDistanceFromUser()
    {
        float[] results = {0,0,0};
        android.location.Location.distanceBetween(this.latitude, this.longitude, BrowseActivity.userLat, BrowseActivity.userLong, results);
        this.distanceFromUser = Float.valueOf(results[0]);
    }
}
