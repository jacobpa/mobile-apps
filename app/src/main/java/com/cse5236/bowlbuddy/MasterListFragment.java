package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cse5236.bowlbuddy.util.Bathroom;
import com.cse5236.bowlbuddy.util.FakeData;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MasterListFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = MasterListFragment.class.getSimpleName();

    private RecyclerView bathroomRecyclerView;
    private RecyclerView.Adapter bathroomAdapter;
    private RecyclerView.LayoutManager bathroomLayoutManager;

    public MasterListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_master_list, container, false);
        Activity activity = getActivity();

        bathroomRecyclerView = v.findViewById(R.id.masterRecyclerView);

        bathroomRecyclerView.setHasFixedSize(true);
        bathroomLayoutManager = new LinearLayoutManager(activity);

        bathroomRecyclerView.setLayoutManager(bathroomLayoutManager);

        NavigationView nav = v.findViewById(R.id.master_nav_view);
        nav.setNavigationItemSelectedListener(this);

        updateUI();

        Log.d(TAG, "onCreateView: View successfully created");
        return v;
    }

    private void updateUI() {
        FakeData data = new FakeData();
        data.generateData(5);

        bathroomAdapter = new BathroomAdapter(data.getBathrooms());
        bathroomRecyclerView.setAdapter(bathroomAdapter);
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
        private Button viewDetails;

        public BathroomHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_bathroom, parent, false));
            // Not using an anonymous class here, as all items use the same onClickListener
            itemView.setOnClickListener(this);

            bathroomTitle = itemView.findViewById(R.id.bathroom_title);
            bathroomDesc = itemView.findViewById(R.id.bathroom_desc);
            ratingBar = itemView.findViewById(R.id.bathroom_overall_rating);
            viewDetails = itemView.findViewById(R.id.view_details_btn);

            viewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetails();
                }
            });

        }

        public void bind(Bathroom bathroom) {
            this.bathroom = bathroom;

            bathroomTitle.setText(bathroom.getLocation().getBuilding().getName());
            bathroomDesc.setText(bathroom.getLocation().toString());
            ratingBar.setRating(bathroom.getAverageRating());
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(
                    getActivity(), "Gender: " + bathroom.getGender(), Toast.LENGTH_SHORT)
                    .show();
        }

        public void openDetails() {
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            startActivity(intent);
        }

    }

    private class BathroomAdapter extends RecyclerView.Adapter<BathroomHolder> {
        private List<Bathroom> bathrooms;

        public BathroomAdapter(List<Bathroom> bathrooms) {
            this.bathrooms = bathrooms;
        }

        @NonNull
        @Override
        public BathroomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new BathroomHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BathroomHolder holder, int position) {
            Bathroom bathroom = bathrooms.get(position);
            holder.bind(bathroom);
        }

        @Override
        public int getItemCount() {
            return bathrooms.size();
        }
    }
}
