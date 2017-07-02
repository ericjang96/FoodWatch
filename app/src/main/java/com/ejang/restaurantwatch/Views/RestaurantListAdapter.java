package com.ejang.restaurantwatch.Views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.ejang.restaurantwatch.Activities.BrowseActivity;
import com.ejang.restaurantwatch.R;
import com.ejang.restaurantwatch.Utils.FilterType;
import com.ejang.restaurantwatch.Utils.HazardRating;
import com.ejang.restaurantwatch.Utils.InspectionResult;
import com.ejang.restaurantwatch.Utils.Restaurant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Eric on 2017-03-22.
 */

public class RestaurantListAdapter extends ArrayAdapter<Restaurant> {

    private BrowseActivity context;
    private ArrayList<Restaurant> allRestaurants;
    private ArrayList<Restaurant> originalRestaurants;

    // Custom ArrayAdapter that handles TrafficEvent objects.
    public RestaurantListAdapter(BrowseActivity context, ArrayList<Restaurant> allRestaurants) {

        super(context, R.layout.list_item_restaurant, allRestaurants);
        this.context = context;
        this.allRestaurants = allRestaurants;
        originalRestaurants = new ArrayList<>(allRestaurants);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView;
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_restaurant, null, true);
        } else {
            rowView = view;
        }

        // Get each view from the TrafficEvent ListView
        TextView nameView = (TextView) rowView.findViewById(R.id.restaurant_name);
        TextView locationView = (TextView) rowView.findViewById(R.id.restaurant_location);
        TextView cleanliness = (TextView) rowView.findViewById(R.id.hazard_level);
        TextView numInspections = (TextView) rowView.findViewById(R.id.num_inspections);
        TextView lastInspectionDate = (TextView) rowView.findViewById(R.id.last_inspection_date);
        TextView distanceFromUser = (TextView) rowView.findViewById(R.id.distance_from_location);

        Restaurant item = getItem(position);

        // Set the values for each view
        nameView.setText(item.name);
        locationView.setText(item.address);

        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");


        ArrayList<InspectionResult> results = item.inspectionResults;
        if (results != null) {
            if (results.get(0).hazardRating == HazardRating.UNSAFE) {
                cleanliness.setText(context.getString(R.string.high_hazard));
                cleanliness.setTextColor(context.getColor(R.color.colorHighHazard));
            } else if (results.get(0).hazardRating == HazardRating.SAFE) {
                cleanliness.setText(context.getString(R.string.low_hazard));
                cleanliness.setTextColor(context.getColor(R.color.colorLowHazard));
            } else {
                cleanliness.setText(context.getString(R.string.mod_hazard));
                cleanliness.setTextColor(context.getColor(R.color.colorModerateHazard));
            }

            numInspections.setText(context.getString(R.string.num_inspections, String.valueOf(results.size())));

            if (lastInspectionDate != null) {
                String date = dateFormat.format(results.get(0).inspectionDate);
                lastInspectionDate.setText(context.getString(R.string.last_inspection_date, date));
            }
        } else {
            cleanliness.setText(context.getString(R.string.unknown_hazard));
            cleanliness.setTextColor(context.getColor(R.color.greyFont));
            numInspections.setText(context.getString(R.string.num_inspections, "Not available"));
            lastInspectionDate.setText(context.getString(R.string.last_inspection_date, "Not available"));
        }

        distanceFromUser.setText(String.format(java.util.Locale.US,"%.1f", item.distanceFromUser / 1000) + " km");

        return rowView;

    }

    @Override
    public RestaurantFilter getFilter() {
        return new RestaurantFilter();
    }

    public void updateDistancesFromUser()
    {
        System.err.println("INSIDE UPDATE DISTANCES");
        // Iterate through all Restaurants and update their distances from user's chosen location
        originalRestaurants.clear();
        System.err.println("THIS COUNT IS: " + this.getCount());
        for (int i=0 ; i < this.getCount() ; i++)
        {
            this.getItem(i).updateDistanceFromUser();
        }
        this.sort(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant o1, Restaurant o2) {
                return o1.distanceFromUser.compareTo(o2.distanceFromUser);
            }
        });
        originalRestaurants.addAll(allRestaurants);
        this.notifyDataSetChanged();
    }

    // Inner class defined to support different types of filtering.
    public class RestaurantFilter extends Filter
    {
        protected FilterType filterType;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            System.err.println("tryna filter dawg");
            if (constraint == null || constraint.length() == 0)
            {
                // No constraint given, so nothing to change in the original array.
                results.values = originalRestaurants;
                results.count = originalRestaurants.size();
            }
            else
            {
                ArrayList<Restaurant> filteredRestaurants = new ArrayList<>();

                for (Restaurant restaurant : originalRestaurants) {
                    System.err.println("constraint: " + constraint.toString());
                    System.err.println("restaurant name: " + restaurant.name);
                    // Filter by matching text in the search box to the restaurant's name.
                    if (filterType == FilterType.TEXT_SEARCH)
                    {
                        if (restaurant.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            System.err.println("satisfied condition");
                            filteredRestaurants.add(restaurant);
                        }
                    }
                    // Filter depending on the safety ratings chosen by the user.
                    else if (filterType == FilterType.SAFETY_RATING)
                    {
                        // If the restaurant doesn't have any inspection data, skip it.
                        if (restaurant.inspectionResults == null)
                        {
                            continue;
                        }
                        if (constraint.toString().contains("safe") && restaurant.inspectionResults.get(0).hazardRating == HazardRating.SAFE)
                        {
                            filteredRestaurants.add(restaurant);
                        }
                        else if (constraint.toString().contains("moderate") && restaurant.inspectionResults.get(0).hazardRating == HazardRating.MODERATE)
                        {
                            filteredRestaurants.add(restaurant);
                        }
                    }
                }
                results.values = filteredRestaurants;
                results.count = filteredRestaurants.size();
            }
            System.err.println("finished filter dawg");
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0) {
                allRestaurants.clear();
                notifyDataSetChanged();
            }
            else {
                allRestaurants.clear();
                allRestaurants.addAll((ArrayList<Restaurant>) results.values);
                notifyDataSetChanged();
            }
        }

        public Filter setFilterType(FilterType filterType)
        {
            this.filterType = filterType;
            return this;
        }
    }
}
