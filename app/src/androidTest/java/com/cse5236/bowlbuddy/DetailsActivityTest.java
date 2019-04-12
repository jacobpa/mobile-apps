package com.cse5236.bowlbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.support.test.rule.ActivityTestRule;

import com.cse5236.bowlbuddy.models.Bathroom;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DetailsActivityTest {
    @Rule
    public ActivityTestRule<DetailsActivity> activityRule =
            new ActivityTestRule<DetailsActivity>(DetailsActivity.class, true, false);

    @Test
    public void testLaunchActivity() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        Bathroom bathroom = new Bathroom();

        bathroom.setCleanRating(5.0f);
        bathroom.setSmellRating(5.0f);
        bathroom.setEmptyRating(5.0f);

        bundle.putSerializable("bathroom", bathroom);
        bundle.putSerializable("favorites", new ArrayList<Bathroom>());

        intent.putExtras(bundle);

        DetailsActivity activity = activityRule.launchActivity(intent);
        assertNotNull(activity);
    }

}
