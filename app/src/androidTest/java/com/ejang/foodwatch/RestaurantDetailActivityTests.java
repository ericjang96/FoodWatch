package com.ejang.foodwatch;


import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ejang.foodwatch.Activities.BrowseActivity;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RestaurantDetailActivityTests {

    @Rule
    public ActivityTestRule<BrowseActivity> mActivityTestRule = new ActivityTestRule<>(BrowseActivity.class);
    private BrowseActivity activity;

    @Before
    public void setUp() throws InterruptedException {
        BrowseActivity.setUserLat(49.191461);
        BrowseActivity.setUserLong(-122.849329);
        BrowseActivity.locationSet.set(true);
        activity = mActivityTestRule.getActivity();
        activity.setDownloadEnabled(false);
        // Clear sharedPreference containing favorite restaurants for this test class
        activity.getSharedPreferences(activity.getString(R.string.shared_pref_fave_list), Context.MODE_PRIVATE).edit().clear().commit();
        // Wait until the ListView is available before interacting with UI
        while (!mActivityTestRule.getActivity().listViewInitialized)
        {
            Thread.sleep(500);
        }
    }

    @Test
    public void testFavoriteRestaurant() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction searchInputView = onView(
                allOf(withId(R.id.search_bar_text),
                        withParent(withId(R.id.search_input_parent)),
                        isDisplayed()));
        searchInputView.perform(typeText("cafe central"), closeSoftKeyboard());

        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(0)
                .onChildView(withId(R.id.restaurant_info))
                .onChildView(allOf(withId(R.id.restaurant_name), withText("Cafe Central")))
                .perform(click(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click the favorite button
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.action_fave), withContentDescription("Fave"), isDisplayed()));
        actionMenuItemView.perform(click());

        // Check that the tracking ID of the restaurant correctly added to the sharedPreference
        String faveRestaurants = activity.getSharedPreferences(activity.getString(R.string.shared_pref_fave_list), Context.MODE_PRIVATE).
                getString(activity.getString(R.string.faved_restaurants), "error");
        Assert.assertEquals("SDFO-8MLMNB", faveRestaurants);

        // Click the unfavorite button
        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.action_fave), withContentDescription("Fave"), isDisplayed()));
        actionMenuItemView2.perform(click());

        // Check that the tracking ID was properly removed
        faveRestaurants = activity.getSharedPreferences(activity.getString(R.string.shared_pref_fave_list), Context.MODE_PRIVATE).
                getString(activity.getString(R.string.faved_restaurants), "error");
        Assert.assertEquals("", faveRestaurants);

    }

    @Test
    public void openViolationsDialogTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction searchInputView = onView(
                allOf(withId(R.id.search_bar_text),
                        withParent(withId(R.id.search_input_parent)),
                        isDisplayed()));
        searchInputView.perform(typeText("seoul grill house"), closeSoftKeyboard());


        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(0)
                .onChildView(withId(R.id.restaurant_info))
                .onChildView(allOf(withId(R.id.restaurant_name), withText("Seoul Grill House")))
                .perform(click(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.inspection_date), withText("September 08, 2015"),
                        childAtPosition(
                                allOf(withId(R.id.restaurant_info),
                                        childAtPosition(
                                                withId(R.id.restaurant_item),
                                                1)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("September 08, 2015")));
        textView.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.alertTitle), withText("Violations"),
                        childAtPosition(
                                allOf(withId(R.id.title_template),
                                        childAtPosition(
                                                withId(R.id.topPanel),
                                                0)),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("Violations")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
