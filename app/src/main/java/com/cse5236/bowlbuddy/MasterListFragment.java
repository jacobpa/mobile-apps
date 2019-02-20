package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cse5236.bowlbuddy.util.Bathroom;
import com.cse5236.bowlbuddy.util.FakeData;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MasterListFragment extends Fragment {
    private final static String TAG = MasterListFragment.class.getSimpleName();

    private RecyclerView bathroomRecyclerView;
    private RecyclerView.Adapter bathroomAdapter;
    private RecyclerView.LayoutManager bathroomLayoutManager;
    private List<Bathroom> bathroomList;

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

        updateUI();

        Log.d(TAG, "onCreateView: View successfully created");
        return v;
    }

    public void updateUI() {
        FakeData data = new FakeData();
        data.generateData(5);

        bathroomAdapter = new BathroomAdapter(data.getBathrooms());
        bathroomRecyclerView.setAdapter(bathroomAdapter);
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
