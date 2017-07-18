package com.ejang.foodwatch.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.ejang.foodwatch.AsyncTasks.DownloadFromWeb;
import com.ejang.foodwatch.AsyncTasks.LoadFromDB;
import com.ejang.foodwatch.BuildConfig;
import com.ejang.foodwatch.R;
import com.ejang.foodwatch.Utils.InspectionResult;
import com.ejang.foodwatch.Utils.Restaurant;
import com.ejang.foodwatch.Views.RestaurantListAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BrowseActivity extends BaseActivity {

    public RestaurantListAdapter restaurantListAdapter;
    public ListView restaurantList;
    public static HashMap<String, ArrayList<InspectionResult>> inspectionData;
    public ArrayList<Restaurant> allRestaurants;
    public static volatile AtomicBoolean locationSet;
    public static volatile AtomicBoolean dataAndAdapterAvailable;
    public static SQLiteDatabase writeableDB;
    public static boolean listViewInitialized;
    public boolean updateCheckerStarted = false;

    private static SharedPreferences sharedPref;
    private static Double userLat;
    private static Double userLong;
    private FloatingActionButton locationFab;
    private Boolean dataUpdateAvailable;

    // This is for testing purposes ONLY to force HTTP connections for testing Volley tasks.
    private Boolean forceWebDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Prevents another activity from being started if another instance of it is already running.
        if (!isTaskRoot())
        {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                System.err.println("Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();
                return;
            }
        }

        super.onCreate(savedInstanceState);

        // Get a database that has read/write access to for this activity. Get the shared pref for
        // checking when the last data update time was.
        writeableDB = dbHelper.getWritableDatabase();
        sharedPref = this.getPreferences(this.MODE_PRIVATE);

        // This is not a useful variable at the moment, but keeping it around just in case I need
        // to use it again in the future.
        locationSet = new AtomicBoolean(false);

        // TODO: I might want to split this into 2 variables because the data could be busy, but the adapter could be free.
        dataAndAdapterAvailable = new AtomicBoolean(true);
        // For now, this field is just for testing purposes.
        listViewInitialized = false;

        // If this value is true, that means the user has selected "No" on the alert dialog last time
        // when asked if they wanted to update their restaurant list. That means that the database
        // is up to date, but the adapter/listview are not. So if this is true, we do not need to
        // update the DB again, but just load the new data into adapter.
        dataUpdateAvailable = false;
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

        setInitialLocation();
        initializeAllRestaurants();
    }

    @Override
    public void onBackPressed() {
        // If drawer is open, back button closes it. If not, return to last content.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            this.moveTaskToBack(true);
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

    // Sets the initial location to use on startup of the app. If there is info from the previous
    // location in sharedPref, use it. If not, use the City of Surrey as the default location.
    private void setInitialLocation() {
        String savedLocationID = sharedPref.getString(getString(R.string.last_saved_location_id), "");
        String locationID;

        if (savedLocationID.length() <= 0)
        {
            locationID = getString(R.string.surrey_city_hall_google_id);
        }
        else
        {
            locationID = savedLocationID;
        }
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API);
        final GoogleApiClient client = builder.build();
        client.connect();
        Places.GeoDataApi.getPlaceById(client, locationID).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places)
            {
                Place surreyCityHall = places.get(0);
                userLat = surreyCityHall.getLatLng().latitude;
                userLong = surreyCityHall.getLatLng().longitude;
                setLocationCaption(surreyCityHall);
                locationSet.set(true);
                places.release();
                client.disconnect();
            }
        });

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    // This method is the entry point for populating the ListView of restaurants. If the DB copyover
    // failed, it makes a HTTP request to the City of Surrey API. This is the worst case scenario,
    // and will take around 1 minute to do the initial fetching/processing. If DB copy is successful,
    // the method quickly populate the adapter and listview. Then we check for updates regularly in
    // the background so the UI thread isn't affected.
    public void initializeAllRestaurants() {
        // Initialize empty hashmap and array that will hold the inspection and restaurant data.
        inspectionData = new HashMap<>();
        allRestaurants = new ArrayList<>();
        restaurantListAdapter = null;
        restaurantList = null;

        // Make loading icon visible
        if (locationSet.get())
        {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        if (BuildConfig.DEBUG)
        {
            // For the purpose of testing Volley. Force a download from Surrey website and return
            if (forceWebDownload)
            {
                downloadRestaurantsAndInspections(false);
                return;
            }
        }

        if (dbCopySuccess)
        {
            // Start an async task to load restaurant and inspection data from the SQLite DB. Even
            // if this data is more than a week old, load something first and update the data in the
            // background so the user has something to see.
            new LoadFromDB(BrowseActivity.this).execute(writeableDB);
        }
        else
        {
            // If DB copy was unsuccessful, download everything from City of Surrey API.
            downloadRestaurantsAndInspections(false);
        }
    }

    private void downloadRestaurantsAndInspections(final Boolean updateQuietly)
    {
        if (!updateQuietly)
        {
            findViewById(R.id.init_bubble).setVisibility(View.VISIBLE);
        }
        // Set up a request to get all inspection data. When response is returned, it starts
        // an async task to organize this data.
        String url = getString(R.string.url_all_inspections);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Start AsyncTask to download/organize the inspection and restaurant data.
                        new DownloadFromWeb(BrowseActivity.this, updateQuietly).execute(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("That didn't work! here is the stacktrace: ");
                error.printStackTrace();
                handleVolleyError(error);

            }
        });

        queue.add(jsonRequest);
        System.err.println("Called first http at: " + System.currentTimeMillis());
    }

    public void initializeListView()
    {
        // Use a copy of allRestaurants to initialize the adapter because allRestaurants is going
        // to be reused. Otherwise, when I modify allRestaurants, it will change the adapter as well
        // because there is a reference to the array I initialized with.
        ArrayList<Restaurant> allRestaurantsCopy = new ArrayList<>(allRestaurants);

        restaurantListAdapter = new RestaurantListAdapter(BrowseActivity.this, allRestaurantsCopy);
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
        restaurantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BrowseActivity.this, RestaurantDetailActivity.class);
                Restaurant resturant = (Restaurant) parent.getItemAtPosition(position);

                // Put all the information that the RestaurantDetailActivity will need in the intent before starting.
                intent.putExtra(getString(R.string.intent_extra_restaurant_lat), resturant.latitude);
                intent.putExtra(getString(R.string.intent_extra_restaurant_long), resturant.longitude);
                intent.putExtra(getString(R.string.intent_extra_restaurant_name), resturant.name);
                intent.putExtra(getString(R.string.intent_extra_restaurant_address), resturant.address);
                intent.putExtra(getString(R.string.intent_extra_restaurant_hazard), resturant.mostRecentSafety);
                intent.putExtra(getString(R.string.intent_extra_restaurant_ID), resturant.trackingID);
                startActivity(intent);
            }
        });
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        System.err.println("Set adapter at: " + System.currentTimeMillis());

        initializeSearchBar();
        listViewInitialized = true;
        dataAndAdapterAvailable.set(true);
    }

    public void initializeSearchBar()
    {
        // Initialize the search bar functionality. If user input text into the bar before the
        // RestaurantListAdapter was initialized, apply the filter right away.
        FloatingSearchView mSearchView = (FloatingSearchView) findViewById(R.id.restaurants_search);
        if (!mSearchView.getQuery().equals(""))
        {
            BrowseActivity.this.restaurantListAdapter.getFilter().filter(mSearchView.getQuery());
        }

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener()
        {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery)
            {
                BrowseActivity.this.restaurantListAdapter.getFilter().filter(newQuery);
            }
        });
        findViewById(R.id.init_bubble).setVisibility(View.GONE);
        findViewById(R.id.anchor).setVisibility(View.GONE);
    }

    // Method called when user selects location with Google Places API.
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
//                findViewById(R.id.no_location_selected_layout).setVisibility(View.GONE);
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                Place place = PlacePicker.getPlace(data, this);
                LatLng latlng = place.getLatLng();
                userLat = latlng.latitude;
                userLong = latlng.longitude;
                locationSet.set(true);

                sharedPref.edit().putString(getString(R.string.last_saved_location_id), place.getId()).commit();
                setLocationCaption(place);
                reorderResults();
            }
        }
    }

    // If the restaurantListAdapter isn't being initialized, it is updated and the listview is reordered
    // according to the new distances from user location. Might want to make this async task to wait
    // for adapter to be not null and available.
    private void reorderResults()
    {
        if (restaurantListAdapter != null)
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

    // Helper to set the location caption above the ListView.
    private void setLocationCaption(Place place)
    {
        TextView location = (TextView) findViewById(R.id.listview_caption);

        String placeName = String.valueOf(place.getName());
        // If name isn't a coordinate, use the name.
        if (!placeName.contains("°") && !placeName.contains("\""))
        {
            location.setText("Restaurants Near " + place.getName());
        }
        // Next best choice is an address if there is one.
        else if (place.getAddress().length() > 0)
        {
            location.setText("Restaurants Near " + place.getAddress());
        }
        // last resort is the coordinates.
        else
        {
            location.setText("Restaurants Near " + place.getName());
        }
    }

    // String array for alert dialog multi choice items
    String[] safetyRatings = new String[]{
            "Safe",
            "Moderate",
            "Unsafe",
            "Unknown"
    };

    // Boolean array for initial selected items
    final boolean[] checkedSafetyRatings = new boolean[]{
            true, // Safe
            true, // Moderate
            true, // Unsafe
            true // Unknown
    };

    // Helper for bringing up the multiple-choice filter dialog when "Filter Search" action is clicked.
    private void showFilterDialog()
    {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        builder.setTitle("Filter by Safety");

        // Set the positive/yes button click listener
        builder.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
                System.err.println("FILTER BY SAFETY: " + String.valueOf(checkedSafetyRatings[2]));
                restaurantListAdapter.getFilter().setIncludeSafe(checkedSafetyRatings[0]);
                restaurantListAdapter.getFilter().setIncludeModerate(checkedSafetyRatings[1]);
                restaurantListAdapter.getFilter().setIncludeUnsafe(checkedSafetyRatings[2]);
                restaurantListAdapter.getFilter().setIncludeUnknown(checkedSafetyRatings[3]);

                FloatingSearchView mSearchView = (FloatingSearchView) findViewById(R.id.restaurants_search);
                BrowseActivity.this.restaurantListAdapter.getFilter().filter(mSearchView.getQuery());
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

    public void showRefreshDialog()
    {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Specify the dialog is cancelable
        builder.setCancelable(true);

        // Set a title for alert dialog
        builder.setTitle("Update Inspection Data");
        builder.setMessage("Check for new data from the City of Surrey and update your restaurants?");

        // Set the positive/yes button click listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
                System.err.println("ADAPTER THREAD IS: " + Thread.currentThread().toString());
                restaurantListAdapter.updateAdapterData(allRestaurants);

                FloatingSearchView mSearchView = (FloatingSearchView) findViewById(R.id.restaurants_search);
                mSearchView.clearQuery();

                Toast toast = Toast.makeText(BrowseActivity.this, "Update was successful", Toast.LENGTH_LONG);
                toast.show();
                // initializeListView();
                dataUpdateAvailable = false;
            }
        });


        // Set the neutral/cancel button click listener
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataUpdateAvailable = true;
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    // Starts the ticker to check if an update is necessary. This ticker is started after the adapter
    // and listview have been initialized on startup to avoid updating while the initial load is still happening.
    public void startUpdateChecker()
    {
        System.err.println("UI THREAD IS: " + Thread.currentThread().toString());
        final Runnable updateChecker = new Runnable() {
            public void run()
            {
                if (dataUpdateAvailable && dataAndAdapterAvailable.get())
                {
                    showRefreshDialog();
                }
                else if (isTimeToUpdate(System.currentTimeMillis()) && dataAndAdapterAvailable.get())
                {
                    System.err.println("QUIET UPDATE STARTED");
                    downloadRestaurantsAndInspections(true);
                }
            }
        };
        if (BuildConfig.DEBUG)
        {
            scheduler.scheduleAtFixedRate(updateChecker, 10, 300, TimeUnit.SECONDS);
        }
        else
        {
            scheduler.scheduleAtFixedRate(updateChecker, 2, 2, TimeUnit.SECONDS);
        }
        updateCheckerStarted = true;
    }

    private boolean isTimeToUpdate(Long epochDate)
    {
        // 604800000 milliseconds = 1 week
        if (BuildConfig.DEBUG)
        {
            return true;
        }
        else
        {
            return System.currentTimeMillis() - epochDate > 604800000;
        }
    }

    public void setInspectionData(HashMap<String, ArrayList<InspectionResult>> inspections)
    {
        inspectionData.clear();
        inspectionData.putAll(inspections);
    }

    public void setRestaurantData(ArrayList<Restaurant> restaurants)
    {
        allRestaurants.clear();
        allRestaurants.addAll(restaurants);
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

    // For testing purposes only.
    private void setForceWebDownload(Boolean forceDownload)
    {
        forceWebDownload = forceDownload;
    }
}
