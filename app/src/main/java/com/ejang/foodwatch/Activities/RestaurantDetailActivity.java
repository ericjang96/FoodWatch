package com.ejang.foodwatch.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ejang.foodwatch.R;
import com.ejang.foodwatch.Utils.HazardRating;
import com.ejang.foodwatch.Utils.InspectionResult;
import com.ejang.foodwatch.Views.InspectionListAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class RestaurantDetailActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intent = getIntent();
        setTitle(intent.getStringExtra(getString(R.string.intent_extra_restaurant_name)));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        ArrayList<InspectionResult> results = BrowseActivity.inspectionData.get(intent.getStringExtra(getString(R.string.intent_extra_restaurant_ID)));
        InspectionListAdapter adapter = new InspectionListAdapter(this, results);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.inspection_linear_layout);

        // Adds all available inspection data to the linear layout.
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, null);
            linearLayout.addView(item);
        }

        // Set the address and hazard details text for the restaurant.
        ((TextView) findViewById(R.id.restaurant_address_text)).setText(intent.getStringExtra(getString(R.string.intent_extra_restaurant_address)) + ", Surrey BC");
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
        // TODO: set according to whats saved in shared pref
        menu.getItem(0).setIcon(R.drawable.ic_action_fave_border);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If refresh button clicked, then fetch data from City of Surrey website regardless of
        // last refresh time.
        if (id == R.id.action_fave)
        {
            if (item.getIcon().getConstantState().equals(getDrawable(R.drawable.ic_action_fave_border).getConstantState()))
            {
                item.setIcon(R.drawable.ic_action_fave);
            }
            else
            {
                item.setIcon(R.drawable.ic_action_fave_border);
            }
        }
        else if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
