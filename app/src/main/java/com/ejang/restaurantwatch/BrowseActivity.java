package com.ejang.restaurantwatch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
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

import android.content.Intent;
import android.widget.Toast;

public class BrowseActivity extends BaseActivity {

    RestaurantListAdapter restaurantListAdapter;
    ListView restaurantList;
    HashMap<String, ArrayList<InspectionResult>> inspectionData;
    ArrayList<Restaurant> allRestaurants;
    static Double userLat;
    static Double userLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set frame content to the correct layout for this activity.
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_browse, contentFrameLayout);
        // Set the current view on the base class and set it to checked.
        super.setCurrentNavView(R.id.nav_restaurant);
        navigationView.setCheckedItem(R.id.nav_restaurant);

        FloatingActionButton locationFab = (FloatingActionButton) findViewById(R.id.fab_location);
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

        return super.onOptionsItemSelected(item);
    }

    public void initializeAllRestaurants() {
        // Reset restaurant data and fetch all info from API
        inspectionData = new HashMap<>();
        allRestaurants = new ArrayList<>();
        restaurantListAdapter = null;
        restaurantList = null;

        // Make loading icon visible
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

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
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            System.err.println("Set adapter at: " + System.currentTimeMillis());
                            restaurantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // TODO: navigate to different page with restaurant details on click
                                }
                            });

                            initializeSearchBar();

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
                BrowseActivity.this.restaurantListAdapter.getFilter().filter(newQuery);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng latlng = place.getLatLng();
                userLat = latlng.latitude;
                userLong = latlng.longitude;
                initializeAllRestaurants();
            }
        }
    }
}