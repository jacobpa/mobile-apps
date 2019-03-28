package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MyReviewsActivity extends AppCompatActivity {
    private final static String TAG = MyReviewsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = new MyReviewsFragment();
        fm.beginTransaction().add(R.id.my_reviews_constraint, fragment).commit();

        Log.d(TAG, "onCreate: Successfully created");
    }
}
