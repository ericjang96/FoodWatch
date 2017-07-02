package com.ejang.restaurantwatch.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ejang.restaurantwatch.R;
import com.ejang.restaurantwatch.SQLDB.DatabaseContract.DatabaseHelper;

// TODO: Implement SQLite DB so we only make HTTP requests when explicitly asked by user
//       each time. Would only update on user command in this case
// TODO: Add another activity for restaurant details when listview item is clicked
// TODO: Add distance from user on listview item + arrow to make it look clickable
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
                    startActivity(anIntent);
                }
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_about:
                if (currentView == null || currentView != R.id.nav_about)
                {
                    Intent anIntent = new Intent(getApplicationContext(), AboutActivity.class);
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
        dbHelper.close();
        super.onDestroy();
    }

    public void setCurrentNavView(Integer layout)
    {
        currentView = layout;
    }
}
