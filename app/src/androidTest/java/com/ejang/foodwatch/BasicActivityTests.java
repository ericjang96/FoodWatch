package com.ejang.foodwatch;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ejang.foodwatch.Activities.BrowseActivity;
import com.ejang.foodwatch.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BasicActivityTests extends EspressoTest {

    @Rule
    public ActivityTestRule<BrowseActivity> mActivityTestRule = new ActivityTestRule<>(BrowseActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        BrowseActivity.setUserLat(49.191461);
        BrowseActivity.setUserLong(-122.849329);
        BrowseActivity.locationSet.set(true);
        mActivityTestRule.getActivity().setDownloadEnabled(false);
        // Wait until the ListView is available before interacting with UI
        while (!mActivityTestRule.getActivity().listViewInitialized)
        {
            Thread.sleep(500);
        }
    }

    @Test
    public void navigationItemsTest() {
        ViewInteraction listView = onView(
                allOf(withId(R.id.restaurant_listview),
                        childAtPosition(
                                allOf(withId(R.id.restaurant_listview_layout),
                                        childAtPosition(
                                                withId(R.id._restaurant_list_layout),
                                                0)),
                                0),
                        isDisplayed()));
        listView.check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("About"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.about_blurb), withText("Thank you for using FoodWatch Beta. \n\nThis app uses restaurant and inspection data from the open data catalogue of City of Surrey. More cities may be added in the future. \n\nFoodWatch is developed by Eric Jang. Feel free to give feedback or report bugs through my Github Issues Page"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container_about),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatCheckedTextView2 = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Restaurants"), isDisplayed()));
        appCompatCheckedTextView2.perform(click());

        ViewInteraction listView2 = onView(
                allOf(withId(R.id.restaurant_listview),
                        childAtPosition(
                                allOf(withId(R.id.restaurant_listview_layout),
                                        childAtPosition(
                                                withId(R.id._restaurant_list_layout),
                                                0)),
                                0),
                        isDisplayed()));
        listView2.check(matches(isDisplayed()));

    }

    @Test
    public void closeDrawerBackButtonTest() {
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        pressBack();

        ViewInteraction textView = onView(
                allOf(withId(R.id.listview_caption),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content_frame),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));
    }
}
