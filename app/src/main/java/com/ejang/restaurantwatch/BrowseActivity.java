package com.ejang.restaurantwatch;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Intent;
import android.widget.TextView;

public class BrowseActivity extends BaseActivity {

    RestaurantListAdapter restaurantListAdapter;
    ListView restaurantList;
    HashMap<String, ArrayList<InspectionResult>> inspectionData;
    ArrayList<Restaurant> allRestaurants;
    static Double userLat;
    static Double userLong;
    static volatile AtomicBoolean locationSet;
    static volatile AtomicBoolean adapterAvailable;
    FloatingActionButton locationFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: instead of initializing this to false every time, make a sharedPreference entry for lat and long. If
        //       those fields exist, then locationSet will be true.
        locationSet = new AtomicBoolean(false);
        adapterAvailable = new AtomicBoolean(false);
        // Set frame content to the correct layout for this activity.
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_browse, contentFrameLayout);
        // Set the current view on the base class and set it to checked.
        super.setCurrentNavView(R.id.nav_restaurant);
        navigationView.setCheckedItem(R.id.nav_restaurant);

        locationFab = (FloatingActionButton) findViewById(R.id.fab_location);
        locationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(BrowseActivity.this), PLACE_PICKER_REQUEST);
                }
                catch (Exception e)
                {
                    return;
                }
            }
        });

        // This will always be true at the moment, but that won't be the case later on. locationSet won't
        // always be initialized to false inside onCreate. Instead, we will check for the value in sharedPreferences
        if (!locationSet.get())
        {
            findViewById(R.id.no_location_selected_layout).setVisibility(View.VISIBLE);
        }

        initializeAllRestaurants();

//        // Add button click listeners.
//        Button filterButton = (Button) findViewById(R.id.button_filter_search);
//        filterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: implement this with alert dialog
//            }
//        });
//
//        Button searchButton = (Button) findViewById(R.id.button_sort_search);
//        filterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: Implement this with alert dialog
//            }
//        });

//        // check if GPS enabled
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    1);
//            System.err.println("you don't got permissions boi");
//        }
//        else
//        {
//            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        }
    }

    @Override
    public void onBackPressed() {
        // If drawer is open, back button closes it. If not, return to last content.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            initializeAllRestaurants();
            return true;
        }

        if (id == R.id.action_filter_search)
        {
            showFilterDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void initializeAllRestaurants() {
        // Initialization of the adapter begins, so prevent it from being modified in other threads.
        adapterAvailable.set(false);
        // Reset restaurant data and fetch all info from API
        inspectionData = new HashMap<>();
        allRestaurants = new ArrayList<>();
        restaurantListAdapter = null;
        restaurantList = null;

        // Make loading icon visible
        if (locationSet.get())
        {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        // Set up a request to get all inspection data. When response is returned, it starts
        // an async task to organize this data.
        String url = getString(R.string.url_all_inspections);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Start AsyncTask to do the organize restaurant data in background.
                        new DownloadAllInspections(BrowseActivity.this).execute(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("That didn't work! here is the stacktrace: ");
                error.printStackTrace();

            }
        });

        queue.add(jsonRequest);
        System.err.println("Called first http at: " + System.currentTimeMillis());
    }

    public void addAllRestaurants() {
        // Set up request to get all inspection data. On response, set the adapter and listview
        // in this UI thread. This isn't heavy-weight enough to need an async task currently.
        String url = getString(R.string.url_all_restaurants);
        RequestQueue queue = Volley.newRequestQueue(this);

        // This method is only called when a location is set, so make the "no location selected" text
        // invisible. Also make the loading panel visible if it isn't at this point.
        findViewById(R.id.no_location_selected_layout).setVisibility(View.GONE);
        if (findViewById(R.id.no_location_selected_layout).getVisibility() != View.VISIBLE)
        {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.err.println("Got second response at: " + System.currentTimeMillis());
                        try {
                            JSONArray restaurants = response.getJSONObject("result").getJSONArray("records");

                            for (int i = 0; i < restaurants.length(); i++) {
                                JSONObject restaurant = restaurants.getJSONObject(i);
                                String name = restaurant.getString("NAME");
                                String addr = restaurant.getString("PHYSICALADDRESS");
                                String latitude = restaurant.getString("LATITUDE");
                                String longitude = restaurant.getString("LONGITUDE");
                                String trackingID = restaurant.getString("TRACKINGNUMBER");

                                if (inspectionData.containsKey(trackingID)) {
                                    Restaurant restaurantEntry = new Restaurant(name, addr, latitude,
                                            longitude, trackingID, inspectionData.get(trackingID));
                                    allRestaurants.add(restaurantEntry);
                                } else {
                                    Restaurant restaurantEntry = new Restaurant(name, addr, latitude,
                                            longitude, trackingID, null);
                                    allRestaurants.add(restaurantEntry);
                                }
                            }
                            System.err.println("Finished second loop at: " + System.currentTimeMillis());
                            Collections.sort(allRestaurants, new Comparator<Restaurant>() {
                                @Override
                                public int compare(Restaurant o1, Restaurant o2) {
                                    return o1.distanceFromUser.compareTo(o2.distanceFromUser);
                                }
                            });
                            restaurantListAdapter = new RestaurantListAdapter(BrowseActivity.this, allRestaurants);
                            restaurantList = (ListView) findViewById(R.id.restaurant_listview);
                            restaurantList.setAdapter(restaurantListAdapter);
                            restaurantList.setOnScrollListener(new AbsListView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                    if (scrollState == SCROLL_STATE_FLING) {
                                        locationFab.hide();
                                    }
                                    else
                                    {
                                        locationFab.show();
                                    }
                                }

                                @Override
                                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                                }
                            });
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            System.err.println("Set adapter at: " + System.currentTimeMillis());

                            initializeSearchBar();
                            // This is the last step of adapter initialization, so make it available again.
                            adapterAvailable.set(true);

                        } catch (JSONException e) {
                            System.err.println("CAUGHT AN EXCEPTION: ");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("That didn't work! here is the stacktrace: ");
                error.printStackTrace();

            }
        });

        queue.add(jsonRequest);
        System.err.println("Made second http request at: " + System.currentTimeMillis());
    }

    public void initializeSearchBar()
    {
        // Initialize the search bar functionality. If user input text into the bar before the
        // RestaurantListAdapter was initialized, apply the filter right away.
        FloatingSearchView mSearchView = (FloatingSearchView) findViewById(R.id.restaurants_search);
        BrowseActivity.this.restaurantListAdapter.getFilter().filter(mSearchView.getQuery());

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener()
        {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery)
            {
                BrowseActivity.this.restaurantListAdapter.getFilter().setFilterType(FilterType.TEXT_SEARCH).filter(newQuery);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                findViewById(R.id.no_location_selected_layout).setVisibility(View.GONE);
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                Place place = PlacePicker.getPlace(data, this);
                LatLng latlng = place.getLatLng();
                userLat = latlng.latitude;
                userLong = latlng.longitude;
                locationSet.set(true);

                TextView location = (TextView) findViewById(R.id.listview_caption);
                location.setText("Restaurants Near " + place.getAddress());

                reorderResults();
            }
        }
    }

    // If the restaurantListAdapter isn't being initialized, it is updated and the listview is reordered
    // according to the new distances from user location.
    protected void reorderResults()
    {
        if (restaurantListAdapter != null && adapterAvailable.get())
        {
            // Modifying the adapter, so make it unavailable to other threads.
            adapterAvailable.set(false);
            restaurantListAdapter.updateDistancesFromUser();
            // Makes the listview scroll to top after updating
            restaurantList.setSelectionAfterHeaderView();
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            // Make adapter available again
            adapterAvailable.set(true);
        }
    }

    // Helper for bringing up the multiple-choice filter dialog when "Filter Search" action is clicked.
    protected void showFilterDialog()
    {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // String array for alert dialog multi choice items
        String[] safetyRatings = new String[]{
                "Safe Restaurants",
                "Moderately Safe Restaurants"
        };

        // Boolean array for initial selected items
        final boolean[] checkedSafetyRatings = new boolean[]{
                false, // Safe
                false, // Moderate

        };

        // Set multiple choice items for alert dialog
        builder.setMultiChoiceItems(safetyRatings, checkedSafetyRatings, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                // Update the current focused item's checked status
                checkedSafetyRatings[which] = isChecked;
            }
        });

        // Specify the dialog is cancelable
        builder.setCancelable(true);

        // Set a title for alert dialog
        builder.setTitle("Filter Results");

        // Set the positive/yes button click listener
        builder.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
                String query = "";
                if (checkedSafetyRatings[0])
                {
                    query = query + "safe";
                }
                if (checkedSafetyRatings[1])
                {
                    query = query + "moderate";
                }
                if (query != "")
                {
                    BrowseActivity.this.restaurantListAdapter.getFilter().setFilterType(FilterType.SAFETY_RATING).filter(query);
                }
            }
        });


        // Set the neutral/cancel button click listener
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click the neutral button
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }
}