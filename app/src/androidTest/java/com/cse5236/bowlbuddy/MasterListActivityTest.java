package com.cse5236.bowlbuddy;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MasterListActivityTest {
    @Rule
    public ActivityTestRule<MasterListActivity> activityRule =
            new ActivityTestRule<MasterListActivity>(MasterListActivity.class, true, false);

    @Test
    public void testLaunchActivity() {
        MasterListActivity activity = activityRule.launchActivity(null);
        MasterListFragment fragment = (MasterListFragment)activity.getSupportFragmentManager().findFragmentById(R.id.loadingScreen);
        assertNotNull(fragment);
        assertNotNull(activity);
    }

}
