package com.ejang.foodwatch;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SimpleBrowseTests extends EspressoTest {

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
    public void searchByTextTest() {

        ViewInteraction searchInputView = onView(
                allOf(withId(R.id.search_bar_text),
                        withParent(withId(R.id.search_input_parent)),
                        isDisplayed()));
        searchInputView.perform(typeText("red robin gourmet burgers (guildford)"), closeSoftKeyboard());

        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(0)
                .onChildView(withId(R.id.restaurant_info))
                .onChildView(withId(R.id.restaurant_location))
                .check(matches(withText("10237 152 St")));

        ViewInteraction editText = onView(
                allOf(withId(R.id.search_bar_text), withText("red robin gourmet burgers (guildford)"),
                        childAtPosition(
                                allOf(withId(R.id.search_input_parent),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
                                                1)),
                                0),
                        isDisplayed()));
        editText.check(matches(withText("red robin gourmet burgers (guildford)")));
    }

    @Test
    public void browseActivityTest() {
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

        ViewInteraction textView = onView(
                allOf(withId(R.id.restaurant_name_text), withText("Cafe Central"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("Cafe Central")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.inspection_date), withText("November 03, 2015"),
                        childAtPosition(
                                allOf(withId(R.id.restaurant_info),
                                        childAtPosition(
                                                withId(R.id.restaurant_item),
                                                1)),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("November 03, 2015")));

    }

    @Test
    public void filterBySafetyTest() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Filter by safety"), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(android.R.id.text1), withText("Safe"),
                        childAtPosition(
                                allOf(withId(R.id.select_dialog_listview),
                                        withParent(withId(R.id.contentPanel))),
                                0),
                        isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatCheckedTextView2 = onView(
                allOf(withId(android.R.id.text1), withText("Unsafe"),
                        childAtPosition(
                                allOf(withId(R.id.select_dialog_listview),
                                        withParent(withId(R.id.contentPanel))),
                                2),
                        isDisplayed()));
        appCompatCheckedTextView2.perform(click());

        ViewInteraction appCompatCheckedTextView3 = onView(
                allOf(withId(android.R.id.text1), withText("Unknown"),
                        childAtPosition(
                                allOf(withId(R.id.select_dialog_listview),
                                        withParent(withId(R.id.contentPanel))),
                                3),
                        isDisplayed()));
        appCompatCheckedTextView3.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("Filter")));
        appCompatButton.perform(scrollTo(), click());

        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(0).
                onChildView(withId(R.id.hazard_level)).check(matches(withText("Moderate hazard")));
        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(1).
                onChildView(withId(R.id.hazard_level)).check(matches(withText("Moderate hazard")));
        onData(anything()).inAdapterView(withId(R.id.restaurant_listview)).atPosition(2).
                onChildView(withId(R.id.hazard_level)).check(matches(withText("Moderate hazard")));

    }

}
