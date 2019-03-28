package com.cse5236.bowlbuddy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ReviewActivity extends AppCompatActivity {
    private final static String TAG = ReviewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Log.d(TAG, "onCreate: Successfully created");
    }
et
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Successfully ended activity");
        finish();
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
