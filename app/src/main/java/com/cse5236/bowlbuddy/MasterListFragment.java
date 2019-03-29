package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MasterListFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = MasterListFragment.class.getSimpleName();
    private static final int UPDATE_FAVORITES_REQUEST = 1;

    private View view;
    private RecyclerView bathroomRecyclerView;
    private RecyclerView.Adapter bathroomAdapter;
    private RecyclerView.LayoutManager bathroomLayoutManager;
    private List<Bathroom> bathroomList;
    private ArrayList<Bathroom> favoritesList;
    private APIService service;
    private SharedPreferences sharedPreferences;
    private FloatingActionButton menuFab;
    private FloatingActionButton addReviewFab;
    private FloatingActionButton gottaGoFab;
    private boolean gottaGoEnabled = false;

    private double latitude = 0;
    private double longitude = 0;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final String DISTANCE_SORT = "Distance";
    private static final String RATING_SORT = "Rating";

    public MasterListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_FAVORITES_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                favoritesList = (ArrayList<Bathroom>) bundle.getSerializable("favorites");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_master_list, container, false);
        Activity activity = getActivity();

        service = APISingleton.getInstance();
        sharedPreferences = activity.getSharedPreferences("Session", Context.MODE_PRIVATE);

        bathroomAdapter = new BathroomAdapter();
        bathroomLayoutManager = new LinearLayoutManager(activity);
        bathroomRecyclerView = view.findViewById(R.id.masterRecyclerView);
        bathroomRecyclerView.setHasFixedSize(true);
        bathroomRecyclerView.setLayoutManager(bathroomLayoutManager);
        bathroomRecyclerView.setAdapter(bathroomAdapter);
        bathroomRecyclerView.setItemViewCacheSize(100);

        NavigationView nav = view.findViewById(R.id.master_nav_view);
        nav.setNavigationItemSelectedListener(this);

        // Initialize the menu, add review, and gotta go fab buttons
        menuFab = view.findViewById(R.id.menu_fab);
        gottaGoFab = view.findViewById(R.id.gotta_go);
        addReviewFab = view.findViewById(R.id.add_review);

        // Set the on click method for clicking the menu FAB
        menuFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabView) {
                if (gottaGoFab.getVisibility() == INVISIBLE) {
                    gottaGoFab.setVisibility(VISIBLE);
                    addReviewFab.setVisibility(VISIBLE);
                } else {
                    gottaGoFab.setVisibility(INVISIBLE);
                    addReviewFab.setVisibility(INVISIBLE);
                }
            }
        });

        // Set the on click method for clicking the gotta go FAB
        gottaGoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabView) {
                startGottaGo();
            }
        });

        // Set the on click method for clicking the add review FAB
        addReviewFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabView) {
                startAddReview();
            }
        });

        service.getAllBathrooms(sharedPreferences.getString("jwt", ""))
                .enqueue(new GetBathroomsCallback(getContext(), view));
        service.getFavorites(sharedPreferences.getInt("id", 0), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetFavoritesCallback(getContext(), view));

        // Declare variable that will manage the calls to the gps for location coordinates
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Declare variable that will be used to listen for responses from the GPS
        locationListener = new UserLocationListener();

        // Check for location permissions before requesting updates from the GPS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                int PERMISSION_REQUEST_LOCATION = 1;
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }

        Log.d(TAG, "onCreateView: View successfully created");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make these FABs invisible when the fragment starts
        gottaGoFab.setVisibility(INVISIBLE);
        addReviewFab.setVisibility(INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (getActivity().checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        } else {
            Snackbar.make(view, "Cannot sort bathrooms by distance without location permissions.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.log_out:
                logOutAction();
                return true;
            case R.id.action_profile:
                launchProfileActivity();
                return true;
            case R.id.my_reviews:
                launchMyReviewsActivity();
                return true;
            case R.id.favorites:
                launchFavoritesActivity();
                return true;
        }

        return true;
    }

    private void launchProfileActivity() {
        Intent i = new Intent(getActivity(), ProfileActivity.class);
        startActivity(i);
    }

    /**
     * "Logs out" a user, by clearing any "Session" Shared Preferences.
     */
    private void logOutAction() {
        getContext().getSharedPreferences("Session", Context.MODE_PRIVATE)
                .edit().clear().apply();

        Intent i = new Intent(getActivity(), LoadingScreenActivity.class);
        startActivity(i);

        getActivity().finish();
    }

    private void launchMyReviewsActivity () {
        Intent i = new Intent(getActivity(), MyReviewsActivity.class);
        startActivity(i);
    }

    private void launchFavoritesActivity() {
        Intent i = new Intent(getActivity(), FavoritesMapActivity.class);
        startActivity(i);
    }

    public void startGottaGo() {

        int color = Color.YELLOW;

        if (gottaGoEnabled) {
            color = Color.WHITE;
        }

        bathroomChanged(bathroomList, DISTANCE_SORT);

        changeRecyclerViewHighlight(color);


        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) bathroomLayoutManager;
        linearLayoutManager.scrollToPositionWithOffset(0,0);

        gottaGoEnabled = !gottaGoEnabled;
    }

    public void startAddReview() {
        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        intent.putExtra("caller", "MasterListFragment");
        startActivity(intent);
    }

    public void changeRecyclerViewHighlight(int color) {
        for (int i = 0; i < 3; i++) {
            Snackbar.make(view, "changing colors: " + color, Snackbar.LENGTH_SHORT).show();
            View bathroom = bathroomRecyclerView.getChildAt(i);
            BathroomHolder holder = (BathroomHolder) bathroomRecyclerView.getChildViewHolder(bathroom);

            bathroom.setBackgroundColor(color);
            holder.getConstraintLayout().setBackgroundColor(color);

        }
//
//        Snackbar.make(view, "changing color: " + iterations, Snackbar.LENGTH_SHORT).show();

//        for (int i = 0; i < 3; i++) {
//            Bathroom bathroom = bathroomList.get(i);
//            for (int j = 0; j < bathroomList.size(); j++) {
//                View child = bathroomRecyclerView.getChildAt(j);
//                BathroomHolder holder = (BathroomHolder) bathroomRecyclerView.getChildViewHolder(child);
//                Bathroom holderBathroom = holder.getBathroom();
//
//                if (bathroom == holderBathroom) {
//                    holder.getConstraintLayout().setBackgroundColor(color);
//                    break;
//                }
//            }
//        }

    }

    public void bathroomChanged(List<Bathroom> unsortedBathroomList, String sortOrder) {
        if (gottaGoEnabled) {
            changeRecyclerViewHighlight(Color.WHITE);
        }

        if (sortOrder.equals(DISTANCE_SORT)) {
            // Order list by bathroom distance
            Collections.sort(unsortedBathroomList, new DistanceSort());
        } else if (sortOrder.equals(RATING_SORT)) {
            // Order list by bathroom rating
            Collections.sort(unsortedBathroomList, new RatingSort());
        }

        bathroomList = unsortedBathroomList;
        bathroomAdapter.notifyDataSetChanged();
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

    private class BathroomHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView bathroomTitle;
        private TextView bathroomDesc;
        private AppCompatRatingBar ratingBar;
        private Bathroom bathroom;
        private ConstraintLayout layout;

        public BathroomHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_bathroom, parent, false));
            // Not using an anonymous class here, as all items use the same onClickListener
            itemView.setOnClickListener(this);

            bathroomTitle = itemView.findViewById(R.id.bathroom_title);
            bathroomDesc = itemView.findViewById(R.id.bathroom_desc);
            ratingBar = itemView.findViewById(R.id.bathroom_overall_rating);
            layout = itemView.findViewById(R.id.recycler_view_constraint_layout);
        }

        public void bind(Bathroom bathroom) {
            this.bathroom = bathroom;
            if (bathroom != null && bathroom.getBuilding() != null) {
                String title = String.format("%s: Floor %d, Room %d",
                        bathroom.getBuilding().getName(),
                        bathroom.getFloor(),
                        bathroom.getRmNum());

                bathroomTitle.setText(title);
                bathroomDesc.setText(bathroom.getBuilding().getAddress());
                ratingBar.setRating(bathroom.getAverageRating());
            }
        }

        @Override
        public void onClick(View view) {
            openDetails();
        }

        public void openDetails() {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bathroom", this.bathroom);
            bundle.putSerializable("favorites", favoritesList);

            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, UPDATE_FAVORITES_REQUEST);
        }

        public String getGender() {
            return bathroom.getGender();
        }

        public Boolean getHandicap() {
            if (bathroom != null) {
                return bathroom.isHandicap();
            } else {
                return false;
            }
        }

        public float getAverageRating() {
            return bathroom.getAverageRating();
        }

        public int getPlyCount() {
            return bathroom.getPlyCount();
        }

        public String getTitle() {
            return bathroomTitle.getText().toString();
        }

        public Bathroom getBathroom() { return this.bathroom; }

        public ConstraintLayout getConstraintLayout() { return this.layout; }
    }

    private class BathroomAdapter extends RecyclerView.Adapter<BathroomHolder> {

        @NonNull
        @Override
        public BathroomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new BathroomHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BathroomHolder holder, int position) {
            Bathroom bathroom = bathroomList.get(position);
            holder.bind(bathroom);
        }

        @Override
        public int getItemCount() {
            if (bathroomList == null)
                return 0;
            return bathroomList.size();
        }


    }

    private class GetBathroomsCallback extends BowlBuddyCallback<List<Bathroom>> {
        public GetBathroomsCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
            if (response.isSuccessful()) {
                bathroomList = response.body();

                for (Bathroom bathroom : bathroomList) {
                    service.getLocation(bathroom.getBuildingID(), sharedPreferences.getString("jwt", ""))
                            .enqueue(new GetBathroomBuildingCallback(getContext(), view, bathroom));
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
                bathroomAdapter.notifyDataSetChanged();
            } else {
                parseError(response);
            }
        }
    }

    private class GetFavoritesCallback extends BowlBuddyCallback<List<Bathroom>> {
        public GetFavoritesCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
            if (response.isSuccessful()) {
                favoritesList = new ArrayList<>(response.body());
            } else {
                parseError(response);
            }
        }
    }
}
