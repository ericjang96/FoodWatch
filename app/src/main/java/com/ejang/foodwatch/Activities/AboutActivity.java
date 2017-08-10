package com.ejang.foodwatch.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.FrameLayout;

import com.ejang.foodwatch.Fragments.AboutFragment;
import com.ejang.foodwatch.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set frame content to the correct layout for this activity.
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.container_about, contentFrameLayout);
        // Initialize the current page with the base fragment
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        AboutFragment aboutFragment = new AboutFragment();
        transaction.add(R.id.container_about, aboutFragment);
        transaction.commit();
        // Set the current view on the base class and set it to checked.
        super.setCurrentNavView(R.id.nav_about);
        navigationView.setCheckedItem(R.id.nav_about);

        //Listen for changes in the back stack
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // Enable Up button only  if there are entries in the back stack
                boolean canback = getFragmentManager().getBackStackEntryCount() > 0;
                if (canback)
                {
                    actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
                    actionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getFragmentManager().popBackStack();
                        }
                    });
                    AboutActivity.this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                else
                {
                    AboutActivity.this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
                    AboutActivity.this.getSupportActionBar().setTitle(getString(R.string.app_about_title));
                }
                actionBarDrawerToggle.syncState();
            }
        });
    }
}
