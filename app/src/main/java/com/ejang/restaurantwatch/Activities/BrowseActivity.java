package com.ejang.restaurantwatch.Activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import com.ejang.restaurantwatch.AsyncTasks.DownloadFromWeb;
import com.ejang.restaurantwatch.AsyncTasks.LoadFromDB;
import com.ejang.restaurantwatch.Utils.FilterType;
import com.ejang.restaurantwatch.Utils.InspectionResult;
import com.ejang.restaurantwatch.R;
import com.ejang.restaurantwatch.Utils.Restaurant;
import com.ejang.restaurantwatch.Views.RestaurantListAdapter;
import com.ejang.restaurantwatch.SQLDB.DatabaseContract;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Intent;
import android.widget.TextView;

public class BrowseActivity extends BaseActivity {

    public RestaurantListAdapter restaurantListAdapter;
    public ListView restaurantList;
    public HashMap<String, ArrayList<InspectionResult>> inspectionData;
    public ArrayList<Restaurant> allRestaurants;
    public static volatile AtomicBoolean locationSet;
    public static volatile AtomicBoolean dataAndAdapterAvailable;
    public static SQLiteDatabase writeableDB;
    public static boolean listViewInitialized;

    private static SharedPreferences sharedPref;
    private static Double userLat;
    private static Double userLong;
    private FloatingActionButton locationFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a database that has read/write access to for this activity. Get the shared pref for
        // checking when the last data update time was.
        writeableDB = dbHelper.getWritableDatabase();
        sharedPref = this.getPreferences(this.MODE_PRIVATE);

        // TODO: instead of initializing this to false every time, make a sharedPreference entry for lat and long. If those fields exist, then locationSet will be true.
        locationSet = new AtomicBoolean(false);
        dataAndAdapterAvailable = new AtomicBoolean(true);
        // For now, this field is just for testing purposes.
        listViewInitialized = false;
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
        int id = item.getItemId();

        // If refresh button clicked, then fetch data from City of Surrey website regardless of
        // last refresh time.
        if (id == R.id.action_refresh) {
            sharedPref.edit().putLong(getString(R.string.last_refresh_time), 0).commit();
            initializeAllRestaurants();
            return true;
        }
        // If filter search button clicked, bring up dialog listing the filter options.
        if (id == R.id.action_filter_search)
        {
            showFilterDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    // This method is the entry point for populating the ListView of restaurants. It decides whether
    // the data should be fetched from the City of Surrey web API or from the local SQLite DB based
    // on the previous update time from the API. If it was more than a week ago, it uses the API.
    public void initializeAllRestaurants() {
        // Initialize empty hashmap and array that will hold the inspection and restaurant data.
        inspectionData = new HashMap<>();
        allRestaurants = new ArrayList<>();
        restaurantListAdapter = null;
        restaurantList = null;
        String lastRefreshTime = getString(R.string.last_refresh_time);

        // Make loading icon visible
        if (locationSet.get())
        {
            findViewById(R.id.no_location_selected_layout).setVisibility(View.GONE);
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        // If the last refresh time from sharedPreferences shows that it's been more than a week
        // since the last data update (from the City of Surrey website) then make HTTP requests
        // and update the local SQLite DB.
        if (isTimeToUpdate(sharedPref.getLong(lastRefreshTime, 0)))
        {
            if (dataAndAdapterAvailable.get())
            {
                dataAndAdapterAvailable.set(false);
                writeableDB.execSQL(DatabaseContract.SQL_CLEAR_RES_TABLE);
                writeableDB.execSQL(DatabaseContract.SQL_CLEAR_INSPECTION_TABLE);
                // This makes async calls, so any statements after this line will run immediately.
                // It will not wait for the tasks to finish.
                downloadRestaurantsAndInspections();
            }
        }
        else
        {
            if (dataAndAdapterAvailable.get())
            {
                dataAndAdapterAvailable.set(false);
                // This makes async calls, so any statements after this line will run immediately.
                // It will not wait for the tasks to finish.
                new LoadFromDB(BrowseActivity.this).execute(writeableDB);
            }
        }
    }

    private void downloadRestaurantsAndInspections()
    {
        // Set up a request to get all inspection data. When response is returned, it starts
        // an async task to organize this data.
        String url = getString(R.string.url_all_inspections);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Start AsyncTask to download/organize the inspection and restaurant data.
                        new DownloadFromWeb(BrowseActivity.this).execute(response);
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
    // according to the new distances from user location. Might want to make this async task to wait
    // for adapter to be not null and available.
    private void reorderResults()
    {
        if (restaurantListAdapter != null && dataAndAdapterAvailable.get())
        {
            // Modifying the adapter, so make it unavailable to other threads.
            dataAndAdapterAvailable.set(false);
            restaurantListAdapter.updateDistancesFromUser();
            // Makes the listview scroll to top after updating
            restaurantList.setSelectionAfterHeaderView();
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            // Make adapter available again
            dataAndAdapterAvailable.set(true);
        }
    }

    // Helper for bringing up the multiple-choice filter dialog when "Filter Search" action is clicked.
    private void showFilterDialog()
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

    public void initializeListView()
    {
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
        listViewInitialized = true;
        dataAndAdapterAvailable.set(true);
    }

    public void addInspectionToMap(InspectionResult inspection)
    {
        // If trackingID key exists, add it. If not, create new key.
        if (inspectionData.containsKey(inspection.trackingID))
        {
            ArrayList<InspectionResult> inspections = inspectionData.get(inspection.trackingID);
            Boolean addedInspection = false;
            Integer initialSize = inspections.size();
            // This loop ensures that the inspections for the same key (trackingID) are organized by the date. Most recent inspection is last in the array.
            for (int i = 0 ; i < initialSize ; i++)
            {
                if(inspections.get(i).inspectionDate.after(inspection.inspectionDate))
                {
                    inspections.add(i, inspection);
                    addedInspection = true;
                    break;
                }
            }
            // Covers the case where the inspection to add is the most recent.
            if (!addedInspection)
            {
                inspections.add(inspection);
            }
        }
        else
        {
            ArrayList<InspectionResult> inspectionResults = new ArrayList<>();
            inspectionResults.add(inspection);
            inspectionData.put(inspection.trackingID, inspectionResults);
        }
    }

    private boolean isTimeToUpdate(Long epochDate)
    {
        // 604800000 milliseconds = 1 week
        return System.currentTimeMillis() - epochDate > 604800000;
    }

    public static Double getUserLat()
    {
        return userLat;
    }

    public static Double getUserLong()
    {
        return userLong;
    }

    public static void setUserLat(Double lat)
    {
        userLat = lat;
    }

    public static void setUserLong(Double longitude)
    {
        userLong = longitude;
    }

    public static SharedPreferences getSharedPref()
    {
        return sharedPref;
    }
}
