package com.ejang.foodwatch.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ejang.foodwatch.R;
import com.ejang.foodwatch.Utils.HazardRating;
import com.ejang.foodwatch.Utils.InspectionResult;
import com.ejang.foodwatch.Views.InspectionListAdapter;
import com.ejang.foodwatch.Views.ViolationListAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class RestaurantDetailActivity extends AppCompatActivity {

    private Intent intent;
    private HashMap<Integer, ViolationListAdapter> adapters;
    private String restaurantTrackingID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intent = getIntent();
        setTitle(intent.getStringExtra(getString(R.string.intent_extra_restaurant_name)));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Disable "Drag" for AppBarLayout (i.e. User can't scroll appBarLayout by directly touching
        // appBarLayout - User can only scroll appBarLayout by only using scrollContent)
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        restaurantTrackingID = intent.getStringExtra(getString(R.string.intent_extra_restaurant_ID));
        ArrayList<InspectionResult> results = BrowseActivity.inspectionData.get(restaurantTrackingID);
        InspectionListAdapter adapter = new InspectionListAdapter(this, results);

        adapters = new HashMap<>();

        // Adds all inspection data for the current restaurant to a linear layout
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.inspection_linear_layout);
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, null);
            linearLayout.addView(item);
            InspectionResult inspection = adapter.getItem(i);

            // Put each inspection's list of violations into a hashmap indexed by its position
            // in the adapter. Necessary to build the violation listview when an inspection item
            // is clicked by the user.
            adapters.put(i, new ViolationListAdapter(this, inspection.violations));

            item.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    // When an inspection item is clicked, build and show an alert dialog with a list
                    // of the violations.
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RestaurantDetailActivity.this);
                    Integer position = ((ViewGroup) v.getParent()).indexOfChild(v);

                    if (adapters.get(position).getCount() < 1)
                    {
                        dialogBuilder.setMessage("No violations");
                    }
                    else
                    {
                        LayoutInflater inflater = RestaurantDetailActivity.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.content_violation, null);
                        ListView violationsList = (ListView) dialogView.findViewById(R.id.listview_violations);
                        violationsList.setAdapter(adapters.get(position));
                        dialogBuilder.setView(dialogView);
                    }

                    dialogBuilder.setPositiveButton("OK", null);
                    dialogBuilder.setTitle("Violations");
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                }
            });
        }

        // Set the address and hazard details text for the restaurant.
        ((TextView) findViewById(R.id.restaurant_address_text)).setText(intent.getStringExtra(getString(R.string.intent_extra_restaurant_address)) + ", Surrey BC");
        ((TextView) findViewById(R.id.restaurant_name_text)).setText(intent.getStringExtra(getString(R.string.intent_extra_restaurant_name)));
        HazardRating hazard = (HazardRating) intent.getSerializableExtra(getString(R.string.intent_extra_restaurant_hazard));
        TextView hazardText = (TextView) findViewById(R.id.restaurant_hazard_text);
        if (hazard == HazardRating.SAFE)
        {
            hazardText.setTextColor(getColor(R.color.colorLowHazard));
            hazardText.setText(getString(R.string.low_hazard));
        }
        else if (hazard == HazardRating.MODERATE)
        {
            hazardText.setTextColor(getColor(R.color.colorModerateHazard));
            hazardText.setText(getString(R.string.mod_hazard));
        }
        else if (hazard == HazardRating.UNSAFE)
        {
            hazardText.setTextColor(getColor(R.color.colorHighHazard));
            hazardText.setText(getString(R.string.high_hazard));
        }
        else
        {
            hazardText.setTextColor(getColor(R.color.greyFont));
            hazardText.setText(getString(R.string.unknown_hazard));
        }

        // Creates a Google map fragment on the layout with a marker at the restaurant location.
        ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Double lat = intent.getDoubleExtra(getString(R.string.intent_extra_restaurant_lat), 0);
                Double longi = intent.getDoubleExtra(getString(R.string.intent_extra_restaurant_long), 0);
                CameraUpdate restaurantLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longi), 15);
                googleMap.moveCamera(restaurantLocation);
                googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, longi)).title(intent.getStringExtra(getString(R.string.intent_extra_restaurant_name))));

                googleMap.getUiSettings().setMapToolbarEnabled(true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurant_detail, menu);

        SharedPreferences faveSharedPref = getSharedPreferences(getString(R.string.shared_pref_fave_list), MODE_PRIVATE);
        String faveRestaurants = faveSharedPref.getString(getString(R.string.faved_restaurants), "");
        if (faveRestaurants.contains(restaurantTrackingID))
        {
            menu.getItem(0).setIcon(R.drawable.ic_action_fave);
        }
        else
        {
            menu.getItem(0).setIcon(R.drawable.ic_action_fave_border);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handles action when user favorites or unfavorites an restaurant.
        if (id == R.id.action_fave)
        {
            if (item.getIcon().getConstantState().equals(getDrawable(R.drawable.ic_action_fave_border).getConstantState()))
            {
                addToFaveList(restaurantTrackingID);
                item.setIcon(R.drawable.ic_action_fave);
                Toast toast = Toast.makeText(this, "Added to Favorites", Toast.LENGTH_LONG);
                toast.show();
            }
            else
            {
                removeFromFaveList(restaurantTrackingID);
                item.setIcon(R.drawable.ic_action_fave_border);
                Toast toast = Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        // Go to the previous activity when app bar's home/up button is pressed.
        else if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Adds a restaurant tracking ID to a shared preference string that represents a comma-separated
    // list of user's favorite restaurants.
    private void addToFaveList(String trackingID)
    {
        SharedPreferences faveSharedPref = getSharedPreferences(getString(R.string.shared_pref_fave_list), MODE_PRIVATE);
        String faveRestaurants = faveSharedPref.getString(getString(R.string.faved_restaurants), "");
        if (faveRestaurants.length() == 0)
        {
            faveSharedPref.edit().putString(getString(R.string.faved_restaurants), trackingID).commit();
        }
        else
        {
            faveSharedPref.edit().putString(getString(R.string.faved_restaurants), faveRestaurants + "," + trackingID).commit();
        }
    }

    // Removes a restaurant tracking ID to a shared preference string that represents a
    // comma-separated list of user's favorite restaurants.
    private void removeFromFaveList(String trackingID)
    {
        SharedPreferences faveSharedPref = getSharedPreferences(getString(R.string.shared_pref_fave_list), MODE_PRIVATE);
        String faveRestaurants = faveSharedPref.getString(getString(R.string.faved_restaurants), "");
        String newFaveList = "";


        Integer index = faveRestaurants.indexOf(trackingID);
        if (index == -1)
        {
            return;
        }
        // Handles cases where tracking ID comes 2nd or later in the comma-separated list
        else if(faveRestaurants.contains("," + trackingID))
        {
            newFaveList = faveRestaurants.replace("," + trackingID, "");
        }
        // Handles cases where tracking ID comes 1st in the comma-separated list
        else if (faveRestaurants.contains(trackingID + ","))
        {
            newFaveList = faveRestaurants.replace(trackingID + ",", "");
        }
        // Handles cases where tracking ID is the only value in the list (i.e. there are no commas)
        else if (faveRestaurants.contains(trackingID))
        {
            newFaveList = faveRestaurants.replace(trackingID, "");
        }

        faveSharedPref.edit().putString(getString(R.string.faved_restaurants), newFaveList).commit();
    }



}
