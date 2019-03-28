package com.cse5236.bowlbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FavoritesMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private final static String TAG = FavoritesMapActivity.class.getSimpleName();
    private View view;
    private List<Bathroom> favoritesList;
    private APIService service;
    private SharedPreferences sharedPreferences;
    private GoogleMap mMap;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_map);

        SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        activity = this;
        view = findViewById(R.id.map);

        service = APISingleton.getInstance();
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        service.getFavorites(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetFavoritesCallback(this, view));

        smf.getMapAsync(this);

        Log.d(TAG, "onCreate: Successfully created");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        Log.d(TAG, "onMapReady: Map successfully ready");
    }

    public void addFavoriteMarkers() {
        LatLng bathroomLocation = new LatLng(0, 0);
        for (Bathroom bathroom : favoritesList) {
            Building bathroomBuilding = bathroom.getBuilding();

            if (bathroomBuilding != null) {
                String title = String.format("%s: Floor %d, Room %d",
                        bathroomBuilding.getName(),
                        bathroom.getFloor(),
                        bathroom.getRmNum());

                bathroomLocation = new LatLng(bathroomBuilding.getLatitude(), bathroomBuilding.getLongitude());
                mMap.addMarker(new MarkerOptions().position(bathroomLocation).title(title));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bathroomLocation));
    }

    private class GetFavoritesCallback extends BowlBuddyCallback<List<Bathroom>> {
        private Context callbackContext;

        public GetFavoritesCallback(Context context, View view) {

            super(context, view);
            this.callbackContext = context;
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
            if (response.isSuccessful()) {
                favoritesList = response.body();

                for (Bathroom bathroom : favoritesList) {
                    service.getLocation(bathroom.getBuildingID(), sharedPreferences.getString("jwt", ""))
                            .enqueue(new GetBuildingCallback(this.callbackContext, view, bathroom));
                }

                Log.d(TAG, "onResponse: Response is " + favoritesList);
            } else {
                parseError(response);
            }
        }

    }

    private class GetBuildingCallback extends BowlBuddyCallback<Building> {
        private Bathroom bathroom;

        public GetBuildingCallback(Context context, View view, Bathroom bathroom) {
            super(context, view);
            this.bathroom = bathroom;
        }

        @Override
        public void onResponse(Call<Building> call, Response<Building> response) {
            if (response.isSuccessful()) {
                this.bathroom.setBuilding(response.body());

                addFavoriteMarkers();
            } else {
                parseError(response);
            }
        }
    }
}
