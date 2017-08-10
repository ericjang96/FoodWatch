package com.ejang.foodwatch;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

import com.ejang.foodwatch.Activities.BrowseActivity;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by eric_ on 2017-08-07.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VolleyTests {

    @Rule
    public ActivityTestRule<BrowseActivity> mActivityTestRule = new ActivityTestRule<>(BrowseActivity.class);

    @Test
    public void browseActivityTest() throws InterruptedException {

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
