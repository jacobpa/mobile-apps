package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MasterListActivity extends AppCompatActivity {
    private final static String TAG = MasterListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_list);

        FragmentManager fm = getSupportFragmentManager();

        // Try to find the Fragment if it's already been created
        Fragment fragment = fm.findFragmentById(R.id.master_list_container);

        if(fragment == null) {
            fragment = new MasterListFragment();
            fm.beginTransaction()
                    .add(R.id.master_list_container, fragment)
                    .commit();
        }
        Log.d(TAG, "onCreate: Successfully created");
    }
}
