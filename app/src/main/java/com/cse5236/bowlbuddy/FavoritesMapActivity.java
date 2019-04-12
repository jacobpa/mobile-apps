package com.cse5236.bowlbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class FavoritesMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private final static String TAG = FavoritesMapActivity.class.getSimpleName();
    private static final int UPDATE_FAVORITES_REQUEST = 1;

    // The view associated with this fragment
    private View view;

    // A list of all the favorite bathrooms
    private List<Bathroom> favoritesList;

    // The APIService used to make queries to the server
    private APIService service;

    // Shared information about the user
    private SharedPreferences sharedPreferences;

    // The instance of the map used to show the locations of favorite bathrooms
    private GoogleMap mMap;

    // The activity this fragment is a child of
    private Activity activity;

    //  The markers currently being plotted on the map
    private List<Marker> activeMarkers = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_map);

        // Get the fragment for the google map
        SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Get the view of the google map
        view = findViewById(R.id.map);

        activity = this;

        // Initialize the service used to communicate with the server
        service = APISingleton.getInstance();

        // Get all of the shared information about the user
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        // Make a request to get all of the user's favorite bathrooms
        service.getFavorites(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetFavoritesCallback(this, view));

        // Initialize the map
        smf.getMapAsync(this);

        Log.d(TAG, "onCreate: Successfully created");
    }

    @Override
    public void onRestart() {
        super.onRestart();

        // Clear map then check for removed favorites
        mMap.clear();

        // Get updated list of favorites
        service.getFavorites(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetFavoritesCallback(this, view));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Initialize the map
        mMap = googleMap;

        // Set the on click listener to this activity
        mMap.setOnMarkerClickListener(this);

        // Check if user has given permissions to access their location
        // If so, enable showing the user's location on the map
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        Log.d(TAG, "onMapReady: Map successfully ready");
    }

    /**
     * Method that adds a favorite bathroom to the map.
     *
     * @param bathroom The bathroom being added to the map
     */
    private void addFavoriteMarkers(Bathroom bathroom) {

        // Get the building the bathroom is in
        Building bathroomBuilding = bathroom.getBuilding();

        // Make sure the building is not null
        if (bathroomBuilding != null) {

            // Set the title of the bathroom
            String title = String.format(Locale.getDefault(), "%s: Floor %d, Room %d",
                    bathroomBuilding.getName(),
                    bathroom.getFloor(),
                    bathroom.getRmNum());

            // Add the bathroom location to the map
            LatLng bathroomLocation = new LatLng(bathroomBuilding.getLatitude(), bathroomBuilding.getLongitude());
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(bathroomLocation).title(title));
            newMarker.setTag(bathroom);
            activeMarkers.add(newMarker);

            // Initialize CameraUpdate variable to zoome to correct place on map
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : activeMarkers) {
                builder.include(marker.getPosition());
            }

            CameraUpdate zoomCamera = CameraUpdateFactory.newLatLngBounds(builder.build(), 200);

            // Move the camera to the area where the bathrooms are shown and zoome the camera
            mMap.moveCamera(zoomCamera);
            mMap.animateCamera(zoomCamera);
        }

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Go the the bathroom details page of the bathroom that was selected
        Bathroom bathroom = (Bathroom) marker.getTag();
        openDetails(bathroom);

        // Return true so no more actions are taken.
        return true;
    }

    /**
     * Method that opens the details page for a selected bathroom
     *
     * @param bathroom The bathroom who's details page will be opened
     */
    private void openDetails(Bathroom bathroom) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bathroom", bathroom);
        bundle.putSerializable("favorites", (Serializable) favoritesList);

        // Create a new intent for the details activity and start the activity
        Intent intent = new Intent(activity, DetailsActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, UPDATE_FAVORITES_REQUEST);
    }

    /**
     * Method that receives the response from the server after making a query for the user's
     * favorite bathrooms.
     */
    private class GetFavoritesCallback extends BowlBuddyCallback<List<Bathroom>> {
        private Context callbackContext;

        GetFavoritesCallback(Context context, View view) {

            super(context, view);
            this.callbackContext = context;
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
            // Check if the request was successful
            if (response.isSuccessful()) {
                // Update the user's favorite bathroom list
                favoritesList = response.body();

                // Send a request to the server to get the buildings for all of the bathrooms
                for (Bathroom bathroom : favoritesList) {
                    service.getLocation(bathroom.getBuildingID(), sharedPreferences.getString("jwt", ""))
                            .enqueue(new GetBuildingCallback(this.callbackContext, view, bathroom));
                }

                Log.d(TAG, "onResponse: Response is " + favoritesList);
            } else {
                // Print error if present
                parseError(response);
            }
        }

    }

    /**
     * Method that receives the response from the server after making a query for the building of
     * the user's favorite bathrooms.
     */
    private class GetBuildingCallback extends BowlBuddyCallback<Building> {
        private Bathroom bathroom;

        GetBuildingCallback(Context context, View view, Bathroom bathroom) {
            super(context, view);
            this.bathroom = bathroom;
        }

        @Override
        public void onResponse(Call<Building> call, Response<Building> response) {
            // Check if the request was successful
            if (response.isSuccessful()) {
                // Store the building to the bathroom class
                this.bathroom.setBuilding(response.body());

                // Add this bathroom to the map
                addFavoriteMarkers(this.bathroom);
            } else {
                // Print error if present
                parseError(response);
            }
        }
    }
}
