package com.example.geocaching1;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SelfActivityTest {

    @Rule
    public ActivityTestRule<SelfActivity> activityRule =
            new ActivityTestRule<>(SelfActivity.class);

    @Before
    public void setup() {
        // 设置 SharedPreferences 以模拟用户登录
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("USERNAME", "TestUser")
                .putString("EMAIL", "test@example.com")
                .putInt("USER_ID", 1)
                .putString("JWT_TOKEN", "dummy_token")
                .apply();
    }


    @Test
    public void testClickMarksItem() {
        // 点击 "Marked Geocaches" 按钮，并检查是否跳转
        onView(withId(R.id.marks_item)).perform(click());
    }

    @Test
    public void testClickFindsItem() {
        // 点击 "Finds" 按钮
        onView(withId(R.id.finds_item)).perform(click());
    }

    @Test
    public void testClickMyCommentsItem() {
        // 点击 "My Comments" 按钮
        onView(withId(R.id.my_comments_item)).perform(click());
    }

    @Test
    public void testClickSettingsItem() {
        // 点击 "Settings" 按钮
        onView(withId(R.id.settings_item)).perform(click());
    }

    @Test
    public void testLogout() {
        // 点击 "Logout" 按钮
        onView(withId(R.id.logout_item)).perform(click());

        // 确保跳转到 EnterActivity
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }
}
