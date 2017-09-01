package com.ejang.foodwatch;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by eric_ on 2017-08-07.
 */

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class MultipleAppTests {

        private static final String FOODWATCH_PACKAGE
                = "com.ejang.foodwatch";
        private static final int LAUNCH_TIMEOUT = 5000;
        private static final String NEW_LISTVIEW_CAPTION = "Restaurants Near King George Station";
        private UiDevice mDevice;

        @Before
        public void startMainActivityFromHomeScreen() {
            // Initialize UiDevice instance
            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

            // Start from the home screen
            mDevice.pressHome();

            // Wait for launcher
            final String launcherPackage = mDevice.getLauncherPackageName();
            assertThat(launcherPackage, notNullValue());
            mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                    LAUNCH_TIMEOUT);

            // Launch the app
            Context context = InstrumentationRegistry.getContext();
            final Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(FOODWATCH_PACKAGE);
            // Clear out any previous instances
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("downloadEnabled", false);
            context.startActivity(intent);


            // Wait for the app to appear
            mDevice.wait(Until.hasObject(By.pkg(FOODWATCH_PACKAGE).depth(0)),
                    LAUNCH_TIMEOUT);
        }

        @Test
        public void testChangeLocation() throws UiObjectNotFoundException {
            // Click the location FAB button on the BrowseActivity UI which takes us to the Google Place Picker
            UiObject locationButton = mDevice.findObject(new UiSelector()
                    .className("android.widget.ImageButton")
                    .resourceId("com.ejang.foodwatch:id/fab_location"));
            locationButton.click();

            // Click the search bar on Google Place Picker
            UiObject googleSearchBar = mDevice.findObject(new UiSelector()
                    .className("android.widget.FrameLayout")
                    .resourceId("com.google.android.gms:id/search_bar"));
            googleSearchBar.click();

            // Type "king george station" in the search bar
            UiObject googleSearchText = mDevice.findObject(new UiSelector()
                    .className("android.widget.EditText")
                    .resourceId("com.google.android.gms:id/edit_text"));
            googleSearchText.setText("king george station");

            // Click the location from the list
            UiObject googleMapLocation = mDevice.findObject(new UiSelector()
                    .className("android.widget.TextView")
                    .resourceId("com.google.android.gms:id/place_autocomplete_prediction_primary_text")
                    .text("King George Station"));
            googleMapLocation.click();

            // Select the location to be the new location to calculate restaurant distance from
            UiObject selectButton = mDevice.findObject(new UiSelector().text("SELECT"));
            selectButton.click();

            // Check that the caption has been updated to reflect the new location
            UiObject listViewCaption = mDevice.findObject(new UiSelector()
                    .className("android.widget.TextView")
                    .resourceId("com.ejang.foodwatch:id/listview_caption"));
            assertThat(listViewCaption.getText(), is(equalTo(NEW_LISTVIEW_CAPTION)));

            // Check that the new ListView has more than 0 children (items in the list)
            UiObject listView = mDevice.findObject(new UiSelector()
                    .className("android.widget.ListView")
                    .resourceId("com.ejang.foodwatch:id/restaurant_listview"));
            Assert.assertTrue(listView.getChildCount() > 0);
        }
}
