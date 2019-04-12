package com.cse5236.bowlbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;
import com.cse5236.bowlbuddy.util.BuildingDBSingleton;

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

    private APIService service;
    private View view;
    private SharedPreferences sharedPrefs;

    private List<Building> buildingList;
    private ArrayAdapter<Building> buildingAdapter;
    private Building building;
    private Bathroom bathroom;

    private Spinner buildingSpn;
    private EditText floorEntry;
    private EditText detailsEntry;
    private EditText roomEntry;
    private Spinner genderSpinner;
    private Switch handicapSwitch;
    private Switch plySwitch;
    private RatingBar smellBar;
    private RatingBar quietBar;
    private RatingBar cleanBar;

    private int smellStars;
    private boolean smellRatingEmpty = true;
    private int quietStars;
    private boolean quietRatingEmpty = true;
    private int cleanStars;
    private boolean cleanRatingEmpty = true;
    private String gender;
    private int floor;
    private int room;
    private int ply = 1;
    boolean handicap = false;

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_review, container, false);

        service = APISingleton.getInstance();
        Intent intent = getActivity().getIntent();
        sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        floorEntry = view.findViewById(R.id.floor_entry);
        roomEntry = view.findViewById(R.id.room_entry);
        buildingSpn = view.findViewById(R.id.building_spinner);
        detailsEntry = view.findViewById(R.id.review_field);
        smellBar = view.findViewById(R.id.smellRating);
        quietBar = view.findViewById(R.id.cleanRating);
        cleanBar = view.findViewById(R.id.quietRating);
        handicapSwitch = view.findViewById(R.id.handicap_switch);
        plySwitch = view.findViewById(R.id.ply_switch);

        // Set up gender spinner
        genderSpinner = view.findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                gender = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        String caller = intent.getStringExtra("caller");
        buildingList = BuildingDBSingleton.getAllBuildings(getContext());

        // Set ArrayAdapter for building spinner
        buildingAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, buildingList);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildingSpn.setAdapter(buildingAdapter);

        // Set listeners for spinner, or disable if not needed
        if (!caller.equals("MasterListFragment")) {
            // Set building selection, disable input
            bathroom = (Bathroom) intent.getSerializableExtra("bathroom");
            buildingList.clear();
            buildingList.add(bathroom.getBuilding());
            buildingSpn.setSelection(0);
            buildingSpn.setEnabled(false);
            building = bathroom.getBuilding();
        } else {
            buildingSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    building = buildingList.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
        }

        if (caller.equals("MasterListFragment")) {
            // If called from the Master List, adding a new bathroom.
            floorEntry.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!TextUtils.isEmpty(editable)) {
                        floor = Integer.parseInt(editable.toString());
                    } else {
                        floor = -1;
                    }
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
                    if (!TextUtils.isEmpty(editable)) {
                        room = Integer.parseInt(editable.toString());
                    } else {
                        room = -1;
                    }
                }
            });
        } else {
            Bundle bundle = intent.getExtras();
            bathroom = (Bathroom) bundle.getSerializable("bathroom");

            // Set floor number, disable input
            if(bathroom.getFloor() != null) {
                floorEntry.setText(bathroom.getFloor().toString());
                floorEntry.setEnabled(false);
            }

            // Set room number, disable input
            if(bathroom.getRmNum() != null) {
                roomEntry.setText(bathroom.getRmNum().toString());
                roomEntry.setEnabled(false);
            }


            // Set gender, disable input
            if(bathroom.getGender() != null) {
                genderSpinner.setSelection(genderAdapter.getPosition(bathroom.getGender()));
                genderSpinner.setEnabled(false);
            }

            if (bathroom.getPlyCount() == 1) {
                plySwitch.setChecked(false);
                plySwitch.toggle();
            } else {
                plySwitch.setChecked(true);
            }

            handicapSwitch.setChecked(bathroom.isHandicap());
        }

        smellBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                smellStars = (int) rating;
                smellRatingEmpty = false;
            }
        });

        quietBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                quietStars = (int) rating;
                quietRatingEmpty = false;
            }
        });

        cleanBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                cleanStars = (int) rating;
                cleanRatingEmpty = false;
            }
        });

        handicapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    handicap = true;
                } else {
                    handicap = false;
                }
            }
        });

        ply = 1;
        plySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ply = 2;
                } else {
                    ply = 1;
                }
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.sendButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReview(building, bathroom);
            }
        });

        return view;
    }

    private void attemptReview(Building building, Bathroom bathroom) {
        Map<String, String> queries = new HashMap<>();
        boolean reviewEmpty = TextUtils.isEmpty(detailsEntry.getText());

        if (cleanRatingEmpty) {
            Snackbar.make(view, "Please rate how clean this bathroom is.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (smellRatingEmpty) {
            Snackbar.make(view, "Please rate how well this bathroom smells.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quietRatingEmpty) {
            Snackbar.make(view, "Please rate how quiet, or empty, this bathroom typically is.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getActivity().getIntent().getStringExtra("caller").equals("MasterListFragment")) {
            // This must be a new bathroom, submit both bathroom and review
            queries.put("floor", Integer.toString(floor));
            queries.put("rmNum", Integer.toString(room));
            queries.put("gender", gender);
            queries.put("cleanRating", Integer.toString(cleanStars));
            queries.put("emptyRating", Integer.toString(quietStars));
            queries.put("smellRating", Integer.toString(smellStars));
            queries.put("handicap", Boolean.toString(handicap));
            queries.put("plyCount", Integer.toString(ply));

            service.addBathroom(building.getId(), queries, sharedPrefs.getString("jwt", "")).
                    enqueue(new AddBathroomCallback(getContext(), view, !reviewEmpty));
        } else {
            queries.put("cleanRating", Integer.toString(cleanStars));
            queries.put("emptyRating", Integer.toString(quietStars));
            queries.put("smellRating", Integer.toString(smellStars));
            queries.put("handicap", Boolean.toString(handicap));
            queries.put("plyCount", Integer.toString(ply));
            service.updateBathroom(building.getId(), bathroom.getId(), queries, sharedPrefs.getString("jwt", "")).enqueue(new AddBathroomCallback(getContext(), view, !reviewEmpty));
        }
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
        private boolean hasReview;

        public AddBathroomCallback(Context context, View view, boolean hasReview) {
            super(context, view);
            this.hasReview = hasReview;
        }

        @Override
        public void onResponse(Call<Bathroom> call, Response<Bathroom> response) {
            if (response.isSuccessful()) {
                Bathroom b = response.body();
                Log.d(TAG, "onResponse: Response is " + response);

                if (hasReview) {
                    service.addReview(sharedPrefs.getInt("id", 0), b.getId(), detailsEntry.getText().toString(), sharedPrefs.getString("jwt", ""))
                            .enqueue(new AddReviewCallback(getContext(), view));
                }

                Snackbar.make(view, "Review sent!", Snackbar.LENGTH_LONG).show();
            } else {
                parseError(response);
            }
        }
    }
}
