package com.cse5236.bowlbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class DetailsActivity extends AppCompatActivity {
    private final static String TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Get an instance of the fragment manager
        FragmentManager fm = getSupportFragmentManager();

        // Try to find the Fragment if it's already been created
        Fragment fragment = fm.findFragmentById(R.id.details_fragment_container);

        if(fragment == null) {
            fragment = new DetailsActivityFragment();
            fm.beginTransaction()
                    .add(R.id.details_fragment_container, fragment)
                    .commit();
        }

        Log.d(TAG, "onCreate: Successfully created");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Successfully ended activity");
    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Successfully paused activity");
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Successfully resumed activity");
    }

}
