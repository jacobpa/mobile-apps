package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MasterListFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = MasterListFragment.class.getSimpleName();

    private View view;
    private RecyclerView bathroomRecyclerView;
    private RecyclerView.Adapter bathroomAdapter;
    private RecyclerView.LayoutManager bathroomLayoutManager;
    private List<Bathroom> bathroomList;
    private APIService service;
    private SharedPreferences sharedPreferences;

    public MasterListFragment() {
        // Required empty public constructor
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

        NavigationView nav = view.findViewById(R.id.master_nav_view);
        nav.setNavigationItemSelectedListener(this);

        service.getAllBathrooms(sharedPreferences.getString("jwt", ""))
                .enqueue(new GetBathroomsCallback(getContext(), view));

        Log.d(TAG, "onCreateView: View successfully created");
        return view;
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

    private class BathroomHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView bathroomTitle;
        private TextView bathroomDesc;
        private AppCompatRatingBar ratingBar;
        private Bathroom bathroom;

        public BathroomHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_bathroom, parent, false));
            // Not using an anonymous class here, as all items use the same onClickListener
            itemView.setOnClickListener(this);

            bathroomTitle = itemView.findViewById(R.id.bathroom_title);
            bathroomDesc = itemView.findViewById(R.id.bathroom_desc);
            ratingBar = itemView.findViewById(R.id.bathroom_overall_rating);
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
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtra("gender", getGender());
            intent.putExtra("handicap", getHandicap());
            intent.putExtra("title", getTitle());
            startActivity(intent);
        }

        public String getGender() {
            return  bathroom.getGender();
        }

        public Boolean getHandicap() {
            if(bathroom != null) {
                return bathroom.isHandicap();
            }
            else {
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
}
