package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FragmentManager fm = getSupportFragmentManager();

        Toolbar tb = findViewById(R.id.profile_toolbar);
        tb.setTitle("User Profile");


        Fragment f = fm.findFragmentById(R.id.profiler_container);
        if(f == null) {
            f = new ProfileFragment();
            fm.beginTransaction()
                    .add(R.id.profiler_container, f)
                    .commit();
        }

    }
}
