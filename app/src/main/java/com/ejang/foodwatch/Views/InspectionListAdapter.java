package com.ejang.foodwatch.Views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ejang.foodwatch.Activities.RestaurantDetailActivity;
import com.ejang.foodwatch.R;
import com.ejang.foodwatch.Utils.HazardRating;
import com.ejang.foodwatch.Utils.InspectionResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by eric_ on 2017-07-16.
 */

public class InspectionListAdapter extends ArrayAdapter<InspectionResult> {

    private RestaurantDetailActivity context;

    // This list is what is actually displayed on the ListView. The superclass holds a reference
    // to this list.
    private ArrayList<InspectionResult> inspectionsInList;

    // Custom ArrayAdapter that handles TrafficEvent objects.
    public InspectionListAdapter(RestaurantDetailActivity context, ArrayList<InspectionResult> inspections) {

        super(context, R.layout.list_item_inspection, inspections);
        this.context = context;
        this.inspectionsInList = inspections;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView;
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_inspection, null, true);
        } else {
            rowView = view;
        }

        // Get the views from the inspection item.
        TextView dateView = (TextView) rowView.findViewById(R.id.inspection_date);
        TextView typeView = (TextView) rowView.findViewById(R.id.inspection_type);
        TextView numViolationsView = (TextView) rowView.findViewById(R.id.num_violations);
        ImageView hazardIcon = (ImageView) rowView.findViewById(R.id.inspection_hazard_icon);


        InspectionResult item = getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String readableDate = dateFormat.format(item.inspectionDate);
        dateView.setText(readableDate);

        typeView.setText(item.inspectionType);

        if (item.numNonCritical + item.numCritical > 0)
        {
            numViolationsView.setText(String.valueOf(item.numCritical + item.numNonCritical) + " violations");
        }
        else
        {
            numViolationsView.setText("No violations");
        }

        HazardRating hazard = item.hazardRating;

        if (hazard == HazardRating.UNSAFE)
        {
            hazardIcon.setImageResource(R.mipmap.ic_thumbs_down);
        }
        else if (hazard == HazardRating.SAFE)
        {
            hazardIcon.setImageResource(R.mipmap.ic_thumbs_up);
        }
        else if (hazard == HazardRating.MODERATE)
        {
            hazardIcon.setImageResource(R.mipmap.ic_thumbs_up_down);
        }
        else
        {
            hazardIcon.setImageResource(R.mipmap.ic_unknown);
        }

        return rowView;

    }
}
