package com.ejang.restaurantwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

// TODO: Add alert dialog UI for filter + implement it
// TODO: Add alert dialog UI for sort + implement + make header reflect it
// TODO: Consider using sharedpreferences for loading arrayadapter if it's faster than making request
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

    public void setCurrentNavView(Integer layout)
    {
        currentView = layout;
    }
}
