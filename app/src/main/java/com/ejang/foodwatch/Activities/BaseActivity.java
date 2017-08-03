package com.ejang.foodwatch.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.ejang.foodwatch.BuildConfig;
import com.ejang.foodwatch.R;
import com.ejang.foodwatch.SQLDB.DatabaseContract.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


// TODO: Add another activity for restaurant details when listview item is clicked
// TODO: Consider uploading to GooglePlay at this point
// TODO: Add notification to navigation bar and allow users to be emailed or sent notifs
// TODO: Add favorite option to filter results. Users can favorite restaurants in the new activity

// This class handles all of the navigation bar and toolbar initialization.
public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    Integer currentView;
    NavigationView navigationView;
    DatabaseHelper dbHelper;
    boolean dbCopySuccess;
    private static Boolean activityVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(this);
        // Set the database helper which will be used to access the DB
        dbHelper = new DatabaseHelper(this);

        try
        {
            // Create a default DB with contents from the DB file in the assets folder. If already
            // exists, does nothing.
            dbHelper.createDefaultDB();
            dbCopySuccess = true;
        }
        catch (IOException e)
        {
            // When the UI thread sees that the copy was unsuccessful, it will download from the web
            // API instead of using the default DB.
            dbCopySuccess = false;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Handles selection of navigation buttons. If button for current activity is pressed,
            // close navigation bar and do nothing.
            case R.id.nav_restaurant:
                if (currentView == null || currentView != R.id.nav_restaurant)
                {
                    Intent anIntent = new Intent(getApplicationContext(), BrowseActivity.class);
                    anIntent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(anIntent);
                }
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_about:
                if (currentView == null || currentView != R.id.nav_about)
                {
                    Intent anIntent = new Intent(getApplicationContext(), AboutActivity.class);
                    anIntent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(anIntent);
                }
                drawerLayout.closeDrawers();
                break;
        }
        return false;
    }

    // Opening and closing database is expensive, so keep it open and close it when the activity
    // is destroyed.
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;
    }

    // Helper for handling all Volley errors.
    public void handleVolleyError(VolleyError error)
    {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Specify the dialog is cancelable
        builder.setCancelable(true);

        // Set a title for alert dialog
        builder.setTitle("Network Error");

        if (error instanceof TimeoutError || error instanceof NoConnectionError)
        {
            builder.setMessage("There was a network error while communicating with the City of Surrey Website. Please make sure your internet works, and try restarting the app.");
        }
        else
        {
            builder.setMessage("Unexpected error occurred while communicating with the City of Surrey Website: " + trimErrorMessage(String.valueOf(error.networkResponse.data), "message"));
        }
        // Set the positive/yes button click listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }

    // Helper to extract and trim the error message.
    public static String trimErrorMessage(String json, String key)
    {
        String trimmedString;
        try
        {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
            // Hopefully it doesn't get to this point ever.
            trimmedString = "Unknown network error";
        }
        if (trimmedString == null || trimmedString.length() == 0)
        {
            trimmedString = "Unknown network error";
        }

        return trimmedString;

    }

    public void setCurrentNavView(Integer layout)
    {
        currentView = layout;
    }

    public static void logDebug(String tag, String message, Throwable error)
    {
        if (BuildConfig.DEBUG)
        {
            if (error != null)
            {
                Log.d(tag, message, error);
            }
            else
            {
                Log.d(tag, message);
            }
        }
    }

    public static Boolean isActivityVisible()
    {
        return activityVisible;
    }
}
