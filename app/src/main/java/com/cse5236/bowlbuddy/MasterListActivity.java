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
    private boolean bathroomChanged = false;
    private Fragment fragment;
    private View view;
    private double latitude = 0;
    private double longitude = 0;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final String DISTANCE_SORT = "Distance";
    private static final String RATING_SORT = "Rating";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_list);

        // Declare variable that will manage the calls to the gps for location coordinates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Declare variable that will be used to listen for responses from the GPS
        locationListener = new UserLocationListener();

        // Check for location permissions before requesting updates from the GPS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                int PERMISSION_REQUEST_LOCATION = 1;
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }

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

        service.getAllBathrooms(sharedPreferences.getString("jwt", ""))
                .enqueue(new GetBathroomsCallback(this, view));

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        } else {
            Snackbar.make(view, "Cannot sort bathrooms by distance without location permissions.", Snackbar.LENGTH_LONG).show();
        }
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

        if (sortOrder.equals(DISTANCE_SORT)) {
            // Order list by bathroom distance
            Collections.sort(bathroomList, new DistanceSort());
        } else if (sortOrder.equals(RATING_SORT)) {
            // Order list by bathroom rating
            Collections.sort(bathroomList, new RatingSort());
        }

        fragment.bathroomChanged(bathroomList);
    }

    class RatingSort implements Comparator<Bathroom>
    {
        public int compare(Bathroom bathroom1, Bathroom bathroom2) {
            int result = 0;
            if (bathroom1.getAverageRating() < bathroom2.getAverageRating()) {
                result = 1;
            } else if (bathroom1.getAverageRating() > bathroom2.getAverageRating()) {
                result = -1;
            }
            return result;
        }
    }

    class DistanceSort implements Comparator<Bathroom> {
        public int compare(Bathroom bathroom1, Bathroom bathroom2) {
            int result = 0;

            if (bathroom1.getBuilding() != null && bathroom2.getBuilding() != null  || !(latitude == 0 && longitude == 0)) {

                Location location1 = new Location("Bathroom 1");
                Location location2 = new Location("Bathroom 2");
                Location userLocation = new Location ("User Location");

                location1.setLatitude(bathroom1.getBuilding().getLatitude());
                location1.setLongitude(bathroom1.getBuilding().getLongitude());

                location2.setLatitude(bathroom2.getBuilding().getLatitude());
                location2.setLongitude(bathroom2.getBuilding().getLongitude());

                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);

                float distance1 = location1.distanceTo(userLocation);
                float distance2 = location2.distanceTo(userLocation);

                if (distance1 < distance2) {
                    result = -1;
                } else if (distance1 > distance2) {
                    result = 1;
                }
            }

            return result;
        }
    }

    private class UserLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location userLocation) {
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
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
                bathroomList = response.body();

                for (Bathroom bathroom : bathroomList) {
                    service.getLocation(bathroom.getBuildingID(), sharedPreferences.getString("jwt", ""))
                            .enqueue(new GetBathroomBuildingCallback(this.callbackContext, view, bathroom));
                }
                Log.d(TAG, "onResponse: Response is " + bathroomList);
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
