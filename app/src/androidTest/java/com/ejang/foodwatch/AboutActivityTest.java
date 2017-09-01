package com.ejang.foodwatch;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ejang.foodwatch.Activities.AboutActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AboutActivityTest extends EspressoTest {

    @Rule
    public ActivityTestRule<AboutActivity> mActivityTestRule = new ActivityTestRule<>(AboutActivity.class);

    @Test
    public void aboutActivityTest() throws InterruptedException {

        ViewInteraction textView = onView(
                allOf(withId(R.id.license_title), withText("Open source licenses"),
                        childAtPosition(
                                allOf(withId(R.id.about_license),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                2)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Open source licenses")));

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.about_license), isDisplayed()));
        linearLayout.perform(click());

        Thread.sleep(3000);

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.alertTitle), withText("Open Source Licenses"),
                        childAtPosition(
                                allOf(withId(R.id.title_template)),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("Open Source Licenses")));

    }
}
