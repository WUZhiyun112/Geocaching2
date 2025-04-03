package com.example.geocaching1;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.geocaching1.model.Geocache;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

@RunWith(AndroidJUnit4.class)
public class GeocacheDetailActivityTest {

    private static final String TEST_GEOCACHE_NAME = "Test Cache";
    private static final String TEST_GEOCACHE_TYPE = "Traditional";
    private static final String TEST_GEOCACHE_DIFFICULTY = "2.5";
    private static final String TEST_GEOCACHE_SIZE = "Small";
    private static final String TEST_GEOCACHE_STATUS = "Active";
    private static final String TEST_GEOCACHE_DESCRIPTION = "<p>This is a test cache description</p>";
    private static final String TEST_GEOCACHE_CODE = "GC12345";
    private static final double TEST_LATITUDE = 39.90469;
    private static final double TEST_LONGITUDE = 116.40717;

    private IdlingResource mIdlingResource;

    @Rule
    public ActivityScenarioRule<GeocacheDetailActivity> activityRule =
            new ActivityScenarioRule<>(createIntentWithTestGeocache());

    @Before
    public void registerIdlingResource() {
        activityRule.getScenario().onActivity(activity -> {
            mIdlingResource = activity.getCountingIdlingResource();
            IdlingRegistry.getInstance().register(mIdlingResource);
        });
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

    private static Intent createIntentWithTestGeocache() {
        // 使用合适的构造函数创建测试Geocache对象
        Geocache testGeocache = new Geocache(
                TEST_GEOCACHE_CODE,
                TEST_GEOCACHE_NAME,
                new BigDecimal(TEST_LATITUDE),
                new BigDecimal(TEST_LONGITUDE),
                TEST_GEOCACHE_STATUS,
                TEST_GEOCACHE_TYPE,
                null, // foundAt
                TEST_GEOCACHE_DESCRIPTION,
                TEST_GEOCACHE_SIZE,
                TEST_GEOCACHE_DIFFICULTY
        );

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeocacheDetailActivity.class);
        intent.putExtra("geocache", testGeocache);
        return intent;
    }

    @Test
    public void testInitialDisplay() {
        // Verify basic information is displayed
        onView(withId(R.id.tv_name))
                .check(matches(withText(TEST_GEOCACHE_NAME)));

        onView(withId(R.id.tv_type))
                .check(matches(withText(containsString(TEST_GEOCACHE_TYPE))));

        onView(withId(R.id.tv_difficulty))
                .check(matches(withText(containsString(TEST_GEOCACHE_DIFFICULTY))));

        onView(withId(R.id.tv_size))
                .check(matches(withText(containsString(TEST_GEOCACHE_SIZE))));

        onView(withId(R.id.tv_status))
                .check(matches(withText(containsString(TEST_GEOCACHE_STATUS))));

        // Verify description is displayed (may need HTML handling)
        onView(withId(R.id.tv_description))
                .check(matches(isDisplayed()));

        // Verify location is displayed
        onView(withId(R.id.tv_location))
                .check(matches(isDisplayed()));

        // Verify map is displayed
        onView(withId(R.id.map_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationButton() {
        // Verify navigate button is present and clickable
        onView(withId(R.id.btn_navigate))
                .check(matches(isDisplayed()))
                .perform(click());

        // Should launch RouteActivity - would need another test to verify
    }

    @Test
    public void testMarkButton() {
        // Verify mark button is present
        onView(withId(R.id.btn_mark))
                .check(matches(isDisplayed()))
                .perform(click());

        // Button state should change - would need mock API responses to fully test
    }

    @Test
    public void testCommentButton() {
        // Verify comment button is present
        onView(withId(R.id.btn_comment))
                .check(matches(isDisplayed()))
                .perform(click());

        // Should launch CommentsActivity - would need another test to verify
    }

    @Test
    public void testStatusChange() {
        // Verify status change text is present
        onView(withId(R.id.tv_change_found_status))
                .check(matches(isDisplayed()))
                .perform(click());

        // Should show dialog - would need to verify dialog contents
    }
}