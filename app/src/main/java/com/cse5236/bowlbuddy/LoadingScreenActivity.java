package com.cse5236.bowlbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cse5236.bowlbuddy.util.APIService;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class LoadingScreenActivity extends AppCompatActivity {
    private final static String TAG = LoadingScreenActivity.class.getSimpleName();
    private APIService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLoggedIn();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen_activity);

        service = new Retrofit.Builder()
                .baseUrl("https://bb.jacobpa.com/api/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(APIService.class);

        HomePageFragment fragment = new HomePageFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.loadingScreen, fragment, fragment.getTag()).commit();
        Log.d(TAG, "onCreate: Successfully created");
    }

    public APIService getService() {
        return service;
    }

    /**
     * Check to see if user is logged in. If they are, and the JWT is present, skip creating
     * the current activity and create the MasterListActivity.
     */
    private void checkLoggedIn() {
        SharedPreferences sharedPrefs = getSharedPreferences("Session", MODE_PRIVATE);
        Log.d(TAG, "checkLoggedIn: jwt: " + sharedPrefs.getString("jwt", ""));

        if (sharedPrefs.contains("jwt")) {
            Intent i = new Intent(this, MasterListActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // Must use Support Library FragmentManager, we're using Support Library Fragments
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate();  // Replace current fragment with last on back stack
        Log.d(TAG, "onBackPressed: Overwritten back task ran");
    }

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
