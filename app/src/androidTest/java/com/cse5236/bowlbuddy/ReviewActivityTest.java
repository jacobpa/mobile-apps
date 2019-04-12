package com.cse5236.bowlbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cse5236.bowlbuddy.models.Bathroom;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ReviewActivityTest {
    @Rule
    public ActivityTestRule<ReviewActivity> activityRule =
            new ActivityTestRule<ReviewActivity>(ReviewActivity.class, true, false);

    @Test
    public void testLaunchActivityFromMasterList() {
        Intent intent = new Intent();
        intent.putExtra("caller", "MasterListFragment");
        ReviewActivity activity = activityRule.launchActivity(intent);
        assertNotNull(activity);

    }

    @Test
    public void testLaunchActivityFromDetails() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        Bathroom bathroom = new Bathroom();

        bathroom.setCleanRating(5.0f);
        bathroom.setSmellRating(5.0f);
        bathroom.setEmptyRating(5.0f);

        bundle.putSerializable("bathroom", bathroom);
        bundle.putString("caller", "DetailsActivityFragment");
        intent.putExtras(bundle);

        ReviewActivity activity = activityRule.launchActivity(intent);

        assertNotNull(activity);

    }

}
