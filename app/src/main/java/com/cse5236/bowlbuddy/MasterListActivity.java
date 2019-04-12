package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import static android.support.v4.print.PrintHelper.ORIENTATION_PORTRAIT;

public class MasterListActivity extends AppCompatActivity {
    private final static String TAG = MasterListActivity.class.getSimpleName();

    private Fragment fragment;

    // Literals used to know how to sort the bathroom list
    private static final String DISTANCE_SORT = "Distance";
    private static final String RATING_SORT = "Rating";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_list);

        // Manually set action bar, with menu button
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        // Only load hamburger menu if in portrait mode
        // For some reason, portrait and landscape are reversed so negation is used
        if (getResources().getConfiguration().orientation != ORIENTATION_PORTRAIT) {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        }

        // Get an instance of the fragment manager
        FragmentManager fm = getSupportFragmentManager();

        // Try to find the Fragment if it's already been created
        fragment = fm.findFragmentById(R.id.master_list_container);

        if(fragment == null) {
            fragment = new MasterListFragment();
            fm.beginTransaction()
                    .add(R.id.master_list_container, fragment)
                    .commit();
        }
        Log.d(TAG, "onCreate: Successfully created");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        // Display the menu bar on the activity
        inflater.inflate(R.menu.menu_toolbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If populated in onCreate, returns null object, wait until created in fragment

        switch(item.getItemId()) {
            // The menu bar on the top left was selected
            case android.R.id.home:
                // This option will only happen in portrait mode
                DrawerLayout drawerLayout = findViewById(R.id.master_drawer);

                if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.distance:
                // Search by closest bathroom
                notifyBathroomListChange(DISTANCE_SORT);
                break;
            case R.id.rating:
                // Search by highest rating
                notifyBathroomListChange(RATING_SORT);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Successfully ended activity");
        //finish();
    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Successfully paused activity");
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Successfully resumed activity");
    }

    private void notifyBathroomListChange(String sortOrder) {

        // Get an instance of the MasterListFragment
        FragmentManager fm = getSupportFragmentManager();
        MasterListFragment fragment = (MasterListFragment) fm.findFragmentById(R.id.master_list_container);

        // Inform the fragment how to sort the list
        fragment.bathroomChanged(sortOrder);
    }

}
