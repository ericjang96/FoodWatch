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

import com.android.volley.VolleyError;
import com.ejang.foodwatch.Activities.BrowseActivity;
import com.ejang.foodwatch.AsyncTasks.DownloadFromWeb;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

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
 * Created by eric_ on 2017-08-07.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VolleyTests extends EspressoTest {

    @Rule
    public ActivityTestRule<BrowseActivity> mActivityTestRule = new ActivityTestRule<>(BrowseActivity.class);

    @Test
    public void updateRestaurantsTest() throws InterruptedException {

        while (!mActivityTestRule.getActivity().updateDialogShown)
        {
            Thread.sleep(500);
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.alertTitle), withText("Update Inspection Data"),
                        childAtPosition(
                                allOf(withId(R.id.title_template),
                                        childAtPosition(
                                                withId(R.id.topPanel),
                                                0)),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("Update Inspection Data")));

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("OK")));
        appCompatButton.perform(scrollTo(), click());

        ListView listView = (ListView) mActivityTestRule.getActivity().findViewById(R.id.restaurant_listview);
        Assert.assertTrue(listView.getCount() > 0);
        Assert.assertTrue(mActivityTestRule.getActivity().updateCheckerStarted);

    }
}
