package com.cse5236.bowlbuddy;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.view.View;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.location.Location.distanceBetween;

public class MasterListActivity extends AppCompatActivity {
    private final static String TAG = MasterListActivity.class.getSimpleName();

    private List<Bathroom> bathroomList;
    private APIService service;
    private SharedPreferences sharedPreferences;
    private Fragment fragment;
    private View view;

    private static final String DISTANCE_SORT = "Distance";
    private static final String RATING_SORT = "Rating";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_list);

//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//        {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
//        } else {
//            int PERMISSION_REQUEST_LOCATION = 1;
//            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
//            }
//        }

        service = APISingleton.getInstance();
        sharedPreferences = this.getSharedPreferences("Session", Context.MODE_PRIVATE);

        view = findViewById(R.id.activity_master_list);

        // Manually set action bar, with menu button
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white);

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
    public void onStart() {
        super.onStart();

        service.getAllBathrooms(sharedPreferences.getString("jwt", ""))
                .enqueue(new GetBathroomsCallback(this, view));
    }

    @Override
    public void onRestart() {
        super.onRestart();

        service.getAllBathrooms(sharedPreferences.getString("jwt", ""))
                .enqueue(new GetBathroomsCallback(this, view));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_toolbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If populated in onCreate, returns null object, wait until created in fragment
        DrawerLayout drawerLayout = findViewById(R.id.master_drawer);

        switch(item.getItemId()) {
            case android.R.id.home:
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

    private void notifyBathroomListChange(String sortOrder) {

        FragmentManager fm = getSupportFragmentManager();
        MasterListFragment fragment = (MasterListFragment) fm.findFragmentById(R.id.master_list_container);

        fragment.bathroomChanged(bathroomList, sortOrder);
    }

    private class GetBathroomsCallback extends BowlBuddyCallback<List<Bathroom>> {
        private Context callbackContext;
        public GetBathroomsCallback(Context context, View view) {
            super(context, view);
            callbackContext = context;
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
            if (response.isSuccessful()) {
                if (bathroomList == null || bathroomList.size() != response.body().size()) {
                    bathroomList = response.body();

                    for (Bathroom bathroom : bathroomList) {
                        service.getLocation(bathroom.getBuildingID(), sharedPreferences.getString("jwt", ""))
                                .enqueue(new GetBathroomBuildingCallback(this.callbackContext, view, bathroom));
                    }
                    Log.d(TAG, "onResponse: Response is " + bathroomList);
                }
            } else {
                parseError(response);
            }
        }
    }

    private class GetBathroomBuildingCallback extends BowlBuddyCallback<Building> {
        private Bathroom bathroom;

        public GetBathroomBuildingCallback(Context context, View view, Bathroom bathroom) {
            super(context, view);
            this.bathroom = bathroom;
        }

        @Override
        public void onResponse(Call<Building> call, Response<Building> response) {
            if (response.isSuccessful()) {
                bathroom.setBuilding(response.body());
                notifyBathroomListChange(DISTANCE_SORT);
            } else {
                parseError(response);
            }
        }
    }

}
