package com.cse5236.bowlbuddy;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoadingScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen_activity);

        HomePageFragment fragment = new HomePageFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.loadingScreen, fragment, fragment.getTag()).commit();

    }

    public void sendMessage(View view)
    {
        Intent intent = new Intent(LoadingScreenActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Must use Support Library FragmentManager, we're using Support Library Fragments
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate();  // Replace current fragment with last on back stack
    }
}
