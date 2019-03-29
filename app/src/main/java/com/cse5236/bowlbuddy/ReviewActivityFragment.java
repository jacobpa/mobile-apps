package com.cse5236.bowlbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewActivityFragment extends Fragment {
    private final static String TAG = ReviewActivityFragment.class.getSimpleName();

    APIService service;
    private View viewVar;

    private Button genderBtn;
    private Button handicapBtn;
    private Button tpBtn;

    private boolean handicap;
    private boolean ply;
    String gender;

    List<Building> buildingList;
    ArrayAdapter<Building> adapter;
    Building building;
    Bathroom bathroom;
    private Spinner buildingSpn;
    private EditText floorEntry;
    private EditText detailsEntry;
    private EditText roomEntry;

    int smellStars;
    int quietStars;
    int cleanStars;
    int floor;
    int room;

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewVar = inflater.inflate(R.layout.fragment_review, container, false);

        service = APISingleton.getInstance();
        Intent intent = getActivity().getIntent();
        final SharedPreferences sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        floorEntry = viewVar.findViewById(R.id.floor_entry);
        roomEntry = viewVar.findViewById(R.id.room_entry);
        buildingSpn = viewVar.findViewById(R.id.building_spinner);
        detailsEntry = viewVar.findViewById(R.id.review_field);
        RatingBar smellBar = viewVar.findViewById(R.id.smellRating);
        RatingBar quietBar = viewVar.findViewById(R.id.cleanRating);
        RatingBar cleanBar = viewVar.findViewById(R.id.quietRating);
        handicapBtn = viewVar.findViewById(R.id.handicapButton);
        genderBtn = viewVar.findViewById(R.id.genderButton);
        tpBtn = viewVar.findViewById(R.id.plyButton);

        String caller = intent.getStringExtra("caller");
        // Populate building spinner
        service.getAllBuildings(sharedPrefs.getString("jwt", "")).enqueue(new GetBuildingsCallback(getContext(), viewVar, !caller.equals("MasterListFragment")));

        if (caller.equals("MasterListFragment")) {
            // If called from the Master List, adding a new bathroom.
            floorEntry.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Nothing
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Nothing
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    floor = Integer.parseInt(editable.toString());
                }
            });
            roomEntry.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    room = Integer.parseInt(editable.toString());
                }
            });
        } else {
            Bundle bundle = intent.getExtras();
            bathroom = (Bathroom) bundle.getSerializable("bathroom");

            // Set floor number, disable input
            floorEntry.setText(bathroom.getFloor().toString());
            floorEntry.setEnabled(false);

            // Set room number, disable input
            roomEntry.setText(bathroom.getRmNum().toString());
            roomEntry.setEnabled(false);
        }

        smellBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                smellStars = (int) rating;
            }
        });

        quietBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                quietStars = (int) rating;
            }
        });

        cleanBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                cleanStars = (int) rating;
            }
        });

        gender = "Male";
        genderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gender.equals("Male")) {
                    genderBtn.setBackgroundColor(Color.parseColor("#ff91f5"));
                    gender = "Female";
                } else {
                    genderBtn.setBackgroundColor(Color.parseColor("#4286f4"));
                    gender = "Male";
                }
            }
        });

        handicap = false;
        handicapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handicap) {
                    handicapBtn.setBackgroundColor(Color.parseColor("#BCBDBD"));
                    handicap = false;
                } else {
                    handicap = true;
                    handicapBtn.setBackgroundColor(Color.parseColor("#4286f4"));
                }
            }
        });

        ply = false;
        tpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ply) {
                    tpBtn.setBackgroundColor(Color.parseColor("#BCBDBD"));
                    ply = false;
                } else {
                    ply = true;
                    tpBtn.setBackgroundColor(Color.parseColor("#4286f4"));
                }
            }
        });

        FloatingActionButton fab = viewVar.findViewById(R.id.sendButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity().getIntent().getStringExtra("caller").equals("MasterListFragment")) {
                    // This must be a new bathroom, submit both bathroom and review
                    Map<String, String> queries = new HashMap<>();
                    queries.put("floor", Integer.toString(floor));
                    queries.put("rmNum", Integer.toString(room));
                    queries.put("gender", gender);
                    queries.put("cleanRating", Integer.toString(cleanStars));
                    queries.put("emptyRating", Integer.toString(quietStars));
                    queries.put("smellRating", Integer.toString(smellStars));

                    service.addBathroom(building.getId(), queries, sharedPrefs.getString("jwt", "")).
                            enqueue(new AddBathroomCallback(getContext(), viewVar));
                } else {
                    service.addReview(sharedPrefs.getInt("id", 0), bathroom.getId(), detailsEntry.getText().toString(), sharedPrefs.getString("jwt", "")).enqueue(new AddReviewCallback(getContext(), viewVar));
                }
                Toast.makeText(getActivity(), "Review Sent", Toast.LENGTH_SHORT).show();
            }
        });

        return viewVar;
    }

    private class AddReviewCallback extends BowlBuddyCallback<Void> {
        public AddReviewCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "onResponse: Response is " + response);
            } else {
                parseError(response);
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            super.onFailure(call, t);
        }
    }

    private class AddBathroomCallback extends BowlBuddyCallback<Bathroom> {
        public AddBathroomCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<Bathroom> call, Response<Bathroom> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "onResponse: Response is " + response);
            } else {
                parseError(response);
            }
        }
    }

    private class GetBuildingsCallback extends BowlBuddyCallback<List<Building>> {
        private boolean bathroomExists;

        public GetBuildingsCallback(Context context, View view, boolean bathroomExists) {
            super(context, view);
            this.bathroomExists = bathroomExists;
        }

        @Override
        public void onResponse(Call<List<Building>> call, Response<List<Building>> response) {
            if (response.isSuccessful()) {
                buildingList = response.body();
                Log.d(TAG, "onResponse: Response is " + buildingList);

                // Sort buildings alphanumerically by title
                Collections.sort(buildingList, new Comparator<Building>() {
                    @Override
                    public int compare(Building building, Building t1) {
                        return building.getName().compareTo(t1.getName());
                    }
                });

                // Set ArrayAdapter for spinner
                adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, buildingList);
                buildingSpn.setAdapter(adapter);

                // Set listeners for spinner, or disable if not needed
                if (bathroomExists) {
                    // Set building selection, disable input
                    buildingSpn.setSelection(adapter.getPosition(bathroom.getBuilding()));
                    buildingSpn.setEnabled(false);
                } else {
                    buildingSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            building = buildingList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // left blank
                        }
                    });
                }
            } else {
                parseError(response);
            }
        }

        @Override
        public void onFailure(Call<List<Building>> call, Throwable t) {
            super.onFailure(call, t);
            Log.d(TAG, t.toString());
        }
    }

}
