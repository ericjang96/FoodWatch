package com.ejang.foodwatch.Activities;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ejang.foodwatch.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set frame content to the correct layout for this activity.
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_about, contentFrameLayout);
        // Set the current view on the base class and set it to checked.
        super.setCurrentNavView(R.id.nav_about);
        navigationView.setCheckedItem(R.id.nav_about);

        LinearLayout openSourceInfo = (LinearLayout) findViewById(R.id.about_license);
        openSourceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLicensesAlertDialog();
            }
        });
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

    private void displayLicensesAlertDialog() {
        WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Open Source Licenses")
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
