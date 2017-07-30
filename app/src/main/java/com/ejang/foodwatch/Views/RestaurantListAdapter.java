package com.ejang.foodwatch.Views;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.ejang.foodwatch.Activities.BrowseActivity;
import com.ejang.foodwatch.Activities.RestaurantDetailActivity;
import com.ejang.foodwatch.R;
import com.ejang.foodwatch.Utils.HazardRating;
import com.ejang.foodwatch.Utils.InspectionResult;
import com.ejang.foodwatch.Utils.Restaurant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Eric on 2017-03-22.
 */

public class RestaurantListAdapter extends ArrayAdapter<Restaurant> {

    private BrowseActivity context;
    
    // This list is what is actually displayed on the ListView. The superclass holds a reference
    // to this list.
    private ArrayList<Restaurant> restaurantsInList;

    // This list should NEVER change in size from the moment it is initialized. It will be sorted
    // throughout its lifetime, but no elements should be added or removed.
    private ArrayList<Restaurant> allOriginalRestaurants;

    // Custom ArrayAdapter that handles TrafficEvent objects.
    public RestaurantListAdapter(BrowseActivity context, ArrayList<Restaurant> restaurants) {

        super(context, R.layout.list_item_restaurant, restaurants);
        this.context = context;
        this.restaurantsInList = restaurants;
        allOriginalRestaurants = new ArrayList<>(restaurantsInList);
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

        // Get each view from the Restaurant ListView
        TextView nameView = (TextView) rowView.findViewById(R.id.restaurant_name);
        TextView locationView = (TextView) rowView.findViewById(R.id.restaurant_location);
        TextView cleanliness = (TextView) rowView.findViewById(R.id.hazard_level);
        TextView numInspections = (TextView) rowView.findViewById(R.id.num_inspections);
        TextView distanceFromUser = (TextView) rowView.findViewById(R.id.distance_from_location);

        Restaurant item = getItem(position);

        // Set the values for each view
        nameView.setText(item.name);
        locationView.setText(item.address);

        HazardRating hazard = item.mostRecentSafety;

        if (hazard == HazardRating.UNSAFE)
        {
            cleanliness.setText(context.getString(R.string.high_hazard));
            cleanliness.setTextColor(context.getColor(R.color.colorHighHazard));
        }
        else if (hazard == HazardRating.SAFE)
        {
            cleanliness.setText(context.getString(R.string.low_hazard));
            cleanliness.setTextColor(context.getColor(R.color.colorLowHazard));
        }
        else if (hazard == HazardRating.MODERATE)
        {
            cleanliness.setText(context.getString(R.string.mod_hazard));
            cleanliness.setTextColor(context.getColor(R.color.colorModerateHazard));
        }
        else
        {
            cleanliness.setText(context.getString(R.string.unknown_hazard));
            cleanliness.setTextColor(context.getColor(R.color.greyFont));
        }

        ArrayList<InspectionResult> results = item.inspectionResults;
        if (results == null || results.size() == 0)
        {
            cleanliness.setText(context.getString(R.string.unknown_hazard));
            cleanliness.setTextColor(context.getColor(R.color.greyFont));
            numInspections.setText(context.getString(R.string.num_inspections, "Not available"));
        }
        else
        {
            numInspections.setText(context.getString(R.string.num_inspections, String.valueOf(results.size())));
        }

        distanceFromUser.setText(getReadableDist(item.distanceFromUser));
        return rowView;

    }

    public static String getReadableDist(Float distance)
    {
        if (distance >= 1000)
        {
            return String.format(java.util.Locale.US,"%.1f", distance / 1000) + " km";
        }
        else
        {
            return String.format(java.util.Locale.US, "%.0f", distance) + " m";
        }
    }

    @Override
    public RestaurantFilter getFilter() {
        return new RestaurantFilter();
    }

    // This method is called when a new location has been set. When a new location is set, all previous
    // search filters are cleared, so we clear the ListView restaurants and populate it with all of
    // the restaurants ordered by distance from the newly set location.
    public void updateDistancesFromUser()
    {
        // Iterate through all Restaurants and update their distances from user's chosen location
        for (int i=0 ; i < allOriginalRestaurants.size() ; i++)
        {
            allOriginalRestaurants.get(i).updateDistanceFromUser();
        }
        Collections.sort(allOriginalRestaurants, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant o1, Restaurant o2) {
                return o1.distanceFromUser.compareTo(o2.distanceFromUser);
            }
        });
        restaurantsInList.clear();
        restaurantsInList.addAll(allOriginalRestaurants);
        this.notifyDataSetChanged();
    }

    public void updateAdapterData(ArrayList<Restaurant> restaurants)
    {
        this.clear();
        this.addAll(restaurants);
        this.allOriginalRestaurants.clear();
        this.allOriginalRestaurants.addAll(restaurants);
        this.notifyDataSetChanged();
    }

    public boolean includeSafe = true;
    protected boolean includeModerate = true;
    protected boolean includeUnsafe = true;
    protected boolean includeUnknown = true;
    protected boolean favesOnly = false;

    // Inner class defined to support different types of filtering.
    public class RestaurantFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            if (constraint == null)
            {
                // No constraint given, so nothing to change in the original array.
                results.values = allOriginalRestaurants;
                results.count = allOriginalRestaurants.size();
            }
            else
            {
                ArrayList<Restaurant> filteredRestaurants = new ArrayList<>();
                ArrayList<String> faveRestaurants = new ArrayList<>();
                if (favesOnly)
                {
                    faveRestaurants = getFaves();
                }
                for (Restaurant restaurant : allOriginalRestaurants) {
                    // Filter by matching text in the search box to the restaurant's name.
                    if (restaurant.name.toLowerCase().contains(constraint.toString().toLowerCase()))
                    {
                        HazardRating hazard = restaurant.mostRecentSafety;
                        if (((includeSafe && hazard == HazardRating.SAFE) || (includeModerate && hazard == HazardRating.MODERATE)
                                || (includeUnsafe && hazard == HazardRating.UNSAFE) || (includeUnknown && hazard == HazardRating.UNKNOWN)))
                        {
                            if (favesOnly && faveRestaurants.contains(restaurant.trackingID) || !favesOnly)
                            {
                                filteredRestaurants.add(restaurant);
                            }
                        }
                    }
                }
                results.values = filteredRestaurants;
                results.count = filteredRestaurants.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0) {
                restaurantsInList.clear();
                notifyDataSetChanged();
            }
            else {
                restaurantsInList.clear();
                restaurantsInList.addAll((ArrayList<Restaurant>) results.values);
                notifyDataSetChanged();
            }
        }

        private ArrayList<String> getFaves()
        {
            SharedPreferences faveSharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref_fave_list), MODE_PRIVATE);
            String faveRestaurants = faveSharedPref.getString(context.getString(R.string.faved_restaurants), "");

            if (faveRestaurants.length() > 0)
            {
                if (faveRestaurants.contains(","))
                {
                    return new ArrayList<>(Arrays.asList(faveRestaurants.split(",")));
                }
                else
                {
                    return new ArrayList<>(Arrays.asList(faveRestaurants));
                }
            }
            else
            {
                return new ArrayList<>();
            }

        }

        public void setIncludeSafe(boolean includeSafe)
        {
            RestaurantListAdapter.this.includeSafe = includeSafe;
        }

        public void setIncludeModerate(boolean includeModerate)
        {
            RestaurantListAdapter.this.includeModerate = includeModerate;
        }

        public void setIncludeUnsafe(boolean includeUnsafe)
        {
            RestaurantListAdapter.this.includeUnsafe = includeUnsafe;
        }

        public void setIncludeUnknown(boolean includeUnknown)
        {
            RestaurantListAdapter.this.includeUnknown = includeUnknown;
        }

        public void setShowFavesOnly(boolean favesOnly)
        {
            RestaurantListAdapter.this.favesOnly = favesOnly;
        }
    }
}

