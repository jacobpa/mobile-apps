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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;
import com.cse5236.bowlbuddy.util.BuildingDBSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java8.util.stream.StreamSupport;
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
    private SwipeRefreshLayout refreshLayout;
    private List<Bathroom> bathroomList;
    private List<Building> buildingList;
    private ArrayList<Bathroom> favoritesList;
    private APIService service;
    private SharedPreferences sharedPreferences;

    // Variables needed for fabs
    private FloatingActionButton menuFab;
    private FloatingActionButton addReviewFab;
    private FloatingActionButton gottaGoFab;
    private boolean gottaGoEnabled = false;

    // Variables needed for distance filter
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

        Activity activity = getActivity();

        if (activity != null) {
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                view = inflater.inflate(R.layout.fragment_master_list_land, container, false);
            } else {

                // Inflate the layout for this fragment
                view = inflater.inflate(R.layout.fragment_master_list, container, false);
            }
        }


        // Initialize variables for communicating with the server and getting user information
        service = APISingleton.getInstance();
        sharedPreferences = activity.getSharedPreferences("Session", Context.MODE_PRIVATE);

        // Initialize the recycler view that will store all of the bathrooms
        bathroomAdapter = new BathroomAdapter();
        bathroomLayoutManager = new LinearLayoutManager(activity);
        bathroomRecyclerView = view.findViewById(R.id.masterRecyclerView);
        bathroomRecyclerView.setHasFixedSize(true);
        bathroomRecyclerView.setLayoutManager(bathroomLayoutManager);
        bathroomRecyclerView.setAdapter(bathroomAdapter);
        bathroomRecyclerView.setItemViewCacheSize(100);

        // Initialize the RefreshLayout
        refreshLayout = view.findViewById(R.id.master_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
            }
        });

        // Initialize the navigation view and set this fragment as it's listener
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

                // Try to get the last known location of user
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                }

            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            // Try to get the last known location of user
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
            }
        }

        if (savedInstanceState != null) {
            buildingList = (List<Building>) savedInstanceState.getSerializable("buildingList");
            bathroomList = (List<Bathroom>) savedInstanceState.getSerializable("bathroomList");
            favoritesList = (ArrayList<Bathroom>) savedInstanceState.getSerializable("favoritesList");
        } else {
            updateList();
            service.getFavorites(sharedPreferences.getInt("id", 0), sharedPreferences.getString("jwt", ""))
                    .enqueue(new GetFavoritesCallback(getContext(), view));
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (bathroomList != null)
            outState.putSerializable("bathroomList", new ArrayList<>(bathroomList));
        if (bathroomList != null)
            outState.putSerializable("buildingList", new ArrayList<>(buildingList));
        if (bathroomList != null)
            outState.putSerializable("favoritesList", favoritesList);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check if location permissions were granted.
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (getActivity().checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        } else {
            // Inform user of decreased functionality if permissions not granted
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

    /**
     * Method used to start an activity to show the user profile
     */
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

    /**
     * Method used to go the the MyReviews activity
     */
    private void launchMyReviewsActivity() {
        Intent i = new Intent(getActivity(), MyReviewsActivity.class);
        startActivity(i);
    }

    /**
     * Method used to go to the FavoritesMap activity
     */
    private void launchFavoritesActivity() {
        Intent i = new Intent(getActivity(), FavoritesMapActivity.class);
        startActivity(i);
    }

    /**
     * Method used to start the gottaGo functionality.  It changes the color of the top 3 bathrooms
     * when it is selected so user knows the closes three bathrooms they can go to.
     */
    public void startGottaGo() {

        // Default the highlight color to yellow
        int color = Color.YELLOW;

        // Change the color to while if changing backgrounds back to white
        if (gottaGoEnabled) {
            color = Color.WHITE;
        }

        // Sort the bathrooms by distance
        bathroomChanged(DISTANCE_SORT);

        // Call method to highlight the top 3 bathrooms
        changeRecyclerViewHighlight(color);

        // Scroll to the top of the recycler view
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) bathroomLayoutManager;
        linearLayoutManager.scrollToPositionWithOffset(0, 0);

        // Change the boolean variable so possible to know which color to highlight bathrooms next
        // time the got to go button is pressed
        gottaGoEnabled = !gottaGoEnabled;
    }

    /**
     * Method used to start the AddReview activity.
     */
    public void startAddReview() {
        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        intent.putExtra("caller", "MasterListFragment");
        startActivity(intent);
    }

    /**
     * Method used to change the background color of a bathroom.
     *
     * @param color The color to change the background to
     */
    public void changeRecyclerViewHighlight(int color) {
        // Change the color for the top 3 bathrooms
        for (int i = 0; i < 3; i++) {
            // Get the view and holder of the bathroom being changed
            View bathroom = bathroomRecyclerView.getChildAt(i);
            BathroomHolder holder = (BathroomHolder) bathroomRecyclerView.getChildViewHolder(bathroom);

            // Change the color of the view and holder of the bathroom
            bathroom.setBackgroundColor(color);
            holder.getConstraintLayout().setBackgroundColor(color);

        }

    }

    /**
     * Method used to start the bathroom sorting process
     *
     * @param sortOrder The order in which the bathrooms will be sorted
     */
    public void bathroomChanged(String sortOrder) {
        // Change bathroom backgrounds before sorting
        if (gottaGoEnabled) {
            changeRecyclerViewHighlight(Color.WHITE);
        }

        if (sortOrder.equals(DISTANCE_SORT)) {
            // Order list by bathroom distance
            Collections.sort(bathroomList, new DistanceSort());
        } else if (sortOrder.equals(RATING_SORT)) {
            // Order list by bathroom rating
            Collections.sort(bathroomList, new RatingSort());
        }

        // Save new bathroom list and notify recycler view of list change
        bathroomAdapter.notifyDataSetChanged();
    }

    public void updateList() {
        if (buildingList == null) {
            new PopulateBuildingsTask().execute();
        } else {
            new PopulateBathroomsTask().execute();
        }
    }

    /**
     * Comparator used to sort bathrooms by overall rating
     */
    class RatingSort implements Comparator<Bathroom> {
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

    /**
     * Comparator used to sort bathrooms by distance from user
     */
    class DistanceSort implements Comparator<Bathroom> {
        public int compare(Bathroom bathroom1, Bathroom bathroom2) {
            int result = 0;

            // Check for the buildings of the users location values being null
            if (bathroom1.getBuilding() != null && bathroom2.getBuilding() != null) {

                // Initialize new location variables for the buildings and the user
                Location location1 = new Location("Bathroom 1");
                Location location2 = new Location("Bathroom 2");
                Location userLocation = new Location("User Location");

                location1.setLatitude(bathroom1.getBuilding().getLatitude());
                location1.setLongitude(bathroom1.getBuilding().getLongitude());

                location2.setLatitude(bathroom2.getBuilding().getLatitude());
                location2.setLongitude(bathroom2.getBuilding().getLongitude());

                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);

                // Get the distance between the user and the buildings
                float distance1 = location1.distanceTo(userLocation);
                float distance2 = location2.distanceTo(userLocation);

                // Determine which building is closer
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
            // Update user location as they move
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private class BathroomHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        /**
         * Method used to open the details activity for the selected bathroom
         */
        public void openDetails() {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bathroom", this.bathroom);
            bundle.putSerializable("favorites", favoritesList);

            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, UPDATE_FAVORITES_REQUEST);
        }

        public ConstraintLayout getConstraintLayout() {
            return this.layout;
        }
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

    private class PopulateBathroomsTask extends AsyncTask<Void, Void, List<Bathroom>> {
        @Override
        protected List<Bathroom> doInBackground(Void... voids) {
            List<Bathroom> bList;

            try {
                Response<List<Bathroom>> bathroomResponse = service.getAllBathrooms(sharedPreferences.getString("jwt", "")).execute();

                if (bathroomResponse.isSuccessful()) {
                    bList = bathroomResponse.body();

                    StreamSupport.stream(bList).forEach(bathroom -> {
                        bathroom.setBuilding(
                                StreamSupport.stream(buildingList).filter(
                                        building -> building.getId().equals(bathroom.getBuildingID())).findFirst().get()
                        );
                    });

                    bathroomList = bList;
                } else {
                    Snackbar.make(view, "Error fetching bathrooms.", Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Bathroom> bathrooms) {
            if (bathroomList != null) {
                bathroomChanged("Distance");
            } else {
                Snackbar.make(view, "Error fetching bathrooms.", Snackbar.LENGTH_LONG).show();
            }

            refreshLayout.setRefreshing(false);
        }
    }

    private class PopulateBuildingsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            buildingList = BuildingDBSingleton.getAllBuildings(getContext());

            if (buildingList == null || buildingList.size() == 0) {
                try {
                    Response<List<Building>> response = service.getAllBuildings(sharedPreferences.getString("jwt", "")).execute();

                    if (response.isSuccessful()) {
                        buildingList = response.body();
                        StreamSupport.stream(buildingList).forEach(building -> {
                            BuildingDBSingleton.addBuilding(getContext(), building);
                        });
                    } else {
                        Snackbar.make(view, "Error fetching buildings.", Snackbar.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Want to populate bathrooms as well now that we have buildings
            new PopulateBathroomsTask().execute();
        }
    }
}
