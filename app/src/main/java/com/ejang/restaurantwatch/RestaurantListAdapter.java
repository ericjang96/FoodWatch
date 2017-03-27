package com.ejang.restaurantwatch;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Eric on 2017-03-22.
 */

public class RestaurantListAdapter extends ArrayAdapter<Restaurant> {

    private Activity context;

    // Custom ArrayAdapter that handles TrafficEvent objects.
    public RestaurantListAdapter(Activity context, ArrayList<Restaurant> jobs) {

        super(context, R.layout.list_item_restaurant, jobs);
        this.context=context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView;
        if (view == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_restaurant, null, true);
        }
        else
        {
            rowView = view;
        }

        // Get each view from the TrafficEvent ListView
        TextView nameView = (TextView) rowView.findViewById(R.id.restaurant_name);
        TextView locationView = (TextView) rowView.findViewById(R.id.restaurant_location);
        TextView cleanliness = (TextView) rowView.findViewById(R.id.hazard_level);
        TextView numInspections = (TextView) rowView.findViewById(R.id.num_inspections);
        TextView lastInspectionDate = (TextView) rowView.findViewById(R.id.last_inspection_date);

        Restaurant item = getItem(position);

        // Set the values for each view
        nameView.setText(item.name);
        locationView.setText(item.address);

        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");


        ArrayList<InspectionResult> results = item.inspectionResults;
        if (results != null)
        {
            if (results.get(0).hazardRating.equalsIgnoreCase("high"))
            {
                cleanliness.setText(context.getString(R.string.high_hazard));
                cleanliness.setTextColor(context.getColor(R.color.colorHighHazard));
            }
            else if (results.get(0).hazardRating.equalsIgnoreCase("low"))
            {
                cleanliness.setText(context.getString(R.string.low_hazard));
                cleanliness.setTextColor(context.getColor(R.color.colorLowHazard));
            }
            else
            {
                cleanliness.setText(context.getString(R.string.mod_hazard));
                cleanliness.setTextColor(context.getColor(R.color.colorModerateHazard));
            }

            numInspections.setText(context.getString(R.string.num_inspections, String.valueOf(results.size())));

            if (lastInspectionDate != null)
            {
                String date = dateFormat.format(results.get(0).inspectionDate);
                lastInspectionDate.setText(context.getString(R.string.last_inspection_date, date));
            }
        }
        else
        {
            cleanliness.setText(context.getString(R.string.unknown_hazard));
            cleanliness.setTextColor(context.getColor(R.color.greyFont));
            numInspections.setText(context.getString(R.string.num_inspections, "Not available"));
            lastInspectionDate.setText(context.getString(R.string.last_inspection_date, "Not available"));
        }

        return rowView;

    }
}

