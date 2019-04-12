package com.cse5236.bowlbuddy;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LoadingScreenActivityTest {
    @Rule
    public ActivityTestRule<LoadingScreenActivity> activityRule =
            new ActivityTestRule<LoadingScreenActivity>(LoadingScreenActivity.class, true, false);

    @Test
    public void testLaunchActivity() {
        LoadingScreenActivity activity = activityRule.launchActivity(null);
        assertNotNull(activity);
        assertEquals(activity.getSupportFragmentManager().getFragments().size(), 1 );
    }

}
