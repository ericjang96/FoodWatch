package com.ejang.foodwatch;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

import com.ejang.foodwatch.Activities.BrowseActivity;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by eric_ on 2017-08-31.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class NoNetworkTests extends EspressoTest {

    @Rule
    public ActivityTestRule<BrowseActivity> mActivityTestRule = new ActivityTestRule<>(BrowseActivity.class);

    private static final String SETTING_PACKAGE = "com.android.settings";
    private static final int LAUNCH_TIMEOUT = 5000;
    private static UiDevice mDevice;

    @BeforeClass
    public static void setup() throws Exception
    {
        toggleAirplaneMode(true);
    }

    @AfterClass
    public static void teardown() throws Exception
    {
        toggleAirplaneMode(false);
    }

    @Test
    public void volleyErrorTest() throws Exception
    {
        while (!mActivityTestRule.getActivity().volleyErrorVisible)
        {
            Thread.sleep(500);
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.alertTitle), withText("Network Error"),
                        childAtPosition(
                                allOf(withId(R.id.title_template),
                                        childAtPosition(
                                                withId(R.id.topPanel),
                                                0)),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("Network Error")));

        ViewInteraction textView2 = onView(
                allOf(withId(android.R.id.message), withText("There was a network error while communicating with the City of Surrey Website. " +
                        "Please make sure your internet works, and try restarting the app.\n\nYou can choose to ignore and continue browsing locally stored data."),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.scrollView),
                                        0),
                                1),
                        isDisplayed()));
        textView2.check(matches(isDisplayed()));
    }

    private static void toggleAirplaneMode(Boolean airplaneModeOn) throws UiObjectNotFoundException {
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
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(SETTING_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT);

        UiObject airplaneModeButton = mDevice.findObject(new UiSelector()
                .className("android.widget.Switch")
                .resourceId("android:id/switch_widget")
                .packageName("com.android.settings"));
        if ((airplaneModeButton.getText().equals("OFF") && airplaneModeOn) || (airplaneModeButton.getText().equals("ON") && !airplaneModeOn)) {
            airplaneModeButton.click();
        }
    }
}
