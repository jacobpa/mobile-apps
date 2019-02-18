package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MasterListFragment extends Fragment {
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

        return v;
    }

    public void updateUI() {
        List<Bathroom> bathrooms = new ArrayList<>(5);
        for(int i = 0; i < 20; i++) {
            bathrooms.add(new Bathroom());
        }

        bathroomAdapter = new BathroomAdapter(bathrooms);
        bathroomRecyclerView.setAdapter(bathroomAdapter);
    }

    private class BathroomHolder extends RecyclerView.ViewHolder {
        public BathroomHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_bathroom, parent, false));
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

        }

        @Override
        public int getItemCount() {
            return bathrooms.size();
        }
    }
}
