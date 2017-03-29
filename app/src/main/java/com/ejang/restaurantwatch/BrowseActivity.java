package com.ejang.restaurantwatch;

import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;

// TODO: Add alert dialog UI for filter + implement it
// TODO: Add alert dialog UI for sort + implement + make header reflect it
// TODO: Consider using sharedpreferences for loading arrayadapter if it's faster than making request
//       each time. Would only update on user command in this case
// TODO: Add another activity for restaurant details when listview item is clicked
// TODO: Add distance from user on listview item + arrow to make it look clickable
// TODO: Consider uploading to GooglePlay at this point
// TODO: Add notification to navigation bar and allow users to be emailed or sent notifs
// TODO: Add favorite option to filter results. Users can favorite restaurants in the new activity

public class BrowseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RestaurantListAdapter restaurantListAdapter;
    ListView restaurantList;
    HashMap<String, ArrayList<InspectionResult>> inspectionData;
    ArrayList<Restaurant> allRestaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeAllRestaurants();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_restaurant) {
            // Handle the camera action

        } else if (id == R.id.nav_about) {
            Intent newAct = new Intent(this, AboutActivity.class);
            startActivity(newAct);

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

}