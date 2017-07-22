package com.ejang.foodwatch;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ejang.foodwatch.Activities.BrowseActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class SimpleBrowseTests {

    @Rule
    public ActivityTestRule<BrowseActivity> mActivityTestRule = new ActivityTestRule<>(BrowseActivity.class);

    // Seems to work as a workaround for https://issuetracker.google.com/issues/37082857
    // Not sure why..
    @After
    public void after() throws InterruptedException {
        Thread.sleep(3000);
    }

    @Before
    public void setExampleLocation()
    {
        BrowseActivity.setUserLat(49.191461);
        BrowseActivity.setUserLong(-122.849329);
        BrowseActivity.locationSet.set(true);
    }

    @Test
    public void searchByTextTest() {

        // Wait until the ListView is available before typing anything in the search bar.
        while (!BrowseActivity.listViewInitialized)
        {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        ViewInteraction searchInputView = onView(
                allOf(withId(R.id.search_bar_text),
                        withParent(withId(R.id.search_input_parent)),
                        isDisplayed()));
        searchInputView.perform(typeText("red robin"), closeSoftKeyboard());

        // Sleep for 5 seconds until the filtering is loaded. Also consider using idling resource here.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(0)
                .onChildView(withId(R.id.restaurant_info))
                .onChildView(withId(R.id.restaurant_location))
                .check(matches(withText("10237 152 St")));

        ViewInteraction editText = onView(
                allOf(withId(R.id.search_bar_text), withText("red robin"),
                        childAtPosition(
                                allOf(withId(R.id.search_input_parent),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
                                                1)),
                                0),
                        isDisplayed()));
        editText.check(matches(withText("red robin")));

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
