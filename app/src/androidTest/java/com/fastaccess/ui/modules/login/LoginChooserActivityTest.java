package com.fastaccess.ui.modules.login;


import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.fastaccess.R;
import com.fastaccess.ui.modules.login.chooser.LoginChooserActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.fastaccess.helper.TestHelper.textInputLayoutHasHint;

@RunWith(AndroidJUnit4.class) @LargeTest
public class LoginChooserActivityTest {

    @Rule public IntentsTestRule<LoginChooserActivity> intentTestRule = new IntentsTestRule<>(LoginChooserActivity.class);

    @Test public void accessTokenButtonTest() {
        onView(withId(R.id.accessToken)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
        onView(withId(R.id.password)).check(matches(textInputLayoutHasHint(intentTestRule.getActivity().getString(R.string.access_token))));
    }
}