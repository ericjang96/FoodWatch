package com.ejang.foodwatch.Views;

import android.text.Html;
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
import com.ejang.foodwatch.Utils.Violation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by eric_ on 2017-07-27.
 */

public class ViolationListAdapter extends ArrayAdapter<Violation> {

    private RestaurantDetailActivity context;

    // This list is what is actually displayed on the ListView. The superclass holds a reference
    // to this list.
    private ArrayList<Violation> violationsInList;

    // Custom ArrayAdapter that handles TrafficEvent objects.
    public ViolationListAdapter(RestaurantDetailActivity context, ArrayList<Violation> violations) {

        super(context, R.layout.list_item_violation, violations);
        this.context = context;
        this.violationsInList = violations;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView;
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_violation, null, true);
        } else {
            rowView = view;
        }

        // Get the views from the inspection item.
        TextView violationDetailView = (TextView) rowView.findViewById(R.id.text_violation_detail);
        TextView violationTypeView = (TextView) rowView.findViewById(R.id.text_violation_type);

        Violation item = getItem(position);

        if (item.getViolationCrit().toLowerCase().contains("not critical"))
        {
            violationTypeView.setTextColor(context.getColor(R.color.colorModerateHazard));
        }
        else
        {
            violationTypeView.setTextColor(context.getColor(R.color.colorHighHazard));
        }
        violationTypeView.setText(item.getViolationCode() + " (" + item.getViolationCrit() + "):");
        violationDetailView.setText(item.getViolationDetail());

        return rowView;
    }
}
