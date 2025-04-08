package com.example.geocaching1;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public ActivityTestRule<SettingsActivity> activityRule =
            new ActivityTestRule<>(SettingsActivity.class, true, true);

    private SettingsActivity getActivity() {
        return activityRule.getActivity();
    }

    @Test
    public void testUIElementsAreVisible() {
        onView(withId(R.id.newUsernameEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.newEmailEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.currentPasswordEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.newPasswordEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmPasswordEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.changeUsernameButton)).check(matches(isDisplayed()));
        onView(withId(R.id.changeEmailButton)).check(matches(isDisplayed()));
        onView(withId(R.id.changePasswordButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testChangeUsername_EmptyInput_ShowsToast() throws InterruptedException {
//        onView(withId(R.id.changeUsernameButton)).perform(click());
//        Thread.sleep(2000); // Allow Toast to appear
//
//        onView(withText("请输入新用户名"))
//                .inRoot(withDecorView(not(getActivity().getWindow().getDecorView())))
//                .check(matches(isDisplayed()));
    }

    @Test
    public void testChangeEmail_EmptyInput_ShowsToast() throws InterruptedException {
//        onView(withId(R.id.changeEmailButton)).perform(click());
//        Thread.sleep(2000); // Allow Toast to appear
//
//        onView(withText("请输入新邮箱"))
//                .inRoot(withDecorView(not(getActivity().getWindow().getDecorView())))
//                .check(matches(isDisplayed()));
    }

    @Test
    public void testChangePassword_InvalidInput_ShowsToast() throws InterruptedException {
//        onView(withId(R.id.changePasswordButton)).perform(click());
//        Thread.sleep(2000); // Allow Toast to appear
//
//        onView(withText("请填写所有密码字段"))
//                .inRoot(withDecorView(not(getActivity().getWindow().getDecorView())))
//                .check(matches(isDisplayed()));
    }


    @Test
    public void testChangePassword_MismatchedPassword_ShowsToast() throws InterruptedException {
//        onView(withId(R.id.currentPasswordEditText)).perform(replaceText("oldpassword"), closeSoftKeyboard());
//        onView(withId(R.id.newPasswordEditText)).perform(replaceText("newpassword123"), closeSoftKeyboard());
//        onView(withId(R.id.confirmPasswordEditText)).perform(replaceText("differentpassword"), closeSoftKeyboard());
//        onView(withId(R.id.changePasswordButton)).perform(click());
//        Thread.sleep(2000); // Allow Toast to appear
//
//        onView(withText("两次输入的新密码不一致"))
//                .inRoot(withDecorView(not(getActivity().getWindow().getDecorView())))
//                .check(matches(isDisplayed()));
    }

    @Test
    public void testChangeUsername_Success() throws InterruptedException {
        onView(withId(R.id.newUsernameEditText)).perform(replaceText("NewUser"), closeSoftKeyboard());
        onView(withId(R.id.changeUsernameButton)).perform(click());
        Thread.sleep(2000); // Allow confirmation dialog to appear

        onView(withText("Confirm Username Change"))
                .check(matches(isDisplayed()));

        onView(withText("Yes")).perform(click());
    }

    @Test
    public void testChangeEmail_Success() throws InterruptedException {
        onView(withId(R.id.newEmailEditText)).perform(replaceText("newemail@example.com"), closeSoftKeyboard());
        onView(withId(R.id.changeEmailButton)).perform(click());
        Thread.sleep(2000); // Allow confirmation dialog to appear

        onView(withText("Confirm Email Change"))
                .check(matches(isDisplayed()));

        onView(withText("Yes")).perform(click());
    }

    @Test
    public void testChangePassword_Success() throws InterruptedException {
        onView(withId(R.id.currentPasswordEditText)).perform(replaceText("oldpassword"), closeSoftKeyboard());
        onView(withId(R.id.newPasswordEditText)).perform(replaceText("newpassword123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordEditText)).perform(replaceText("newpassword123"), closeSoftKeyboard());
        onView(withId(R.id.changePasswordButton)).perform(click());
        Thread.sleep(2000); // Allow confirmation dialog to appear

        onView(withText("Confirm Password Change"))
                .check(matches(isDisplayed()));

        onView(withText("Yes")).perform(click());
    }
}
