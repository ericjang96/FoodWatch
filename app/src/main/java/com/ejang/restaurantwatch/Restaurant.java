package com.ejang.restaurantwatch;

import java.util.ArrayList;

/**
 * Created by Eric on 2017-03-22.
 */

public class Restaurant {

    public String name;
    public String address;
    public String latitude;
    public String longitude;
    public String trackingID;
    public ArrayList<InspectionResult> inspectionResults;

    public Restaurant(String name, String address, String latitude, String longitude, String trackingID, ArrayList<InspectionResult> inspections)
    {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.trackingID = trackingID;
        this.inspectionResults = inspections;
    }
}
