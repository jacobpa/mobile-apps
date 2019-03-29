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

    private APIService service;
    private View viewVar;
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
        viewVar = inflater.inflate(R.layout.fragment_review, container, false);

        service = APISingleton.getInstance();
        Intent intent = getActivity().getIntent();
        sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        floorEntry = viewVar.findViewById(R.id.floor_entry);
        roomEntry = viewVar.findViewById(R.id.room_entry);
        buildingSpn = viewVar.findViewById(R.id.building_spinner);
        detailsEntry = viewVar.findViewById(R.id.review_field);
        RatingBar smellBar = viewVar.findViewById(R.id.smellRating);
        RatingBar quietBar = viewVar.findViewById(R.id.cleanRating);
        RatingBar cleanBar = viewVar.findViewById(R.id.quietRating);
        handicapSwitch = viewVar.findViewById(R.id.handicap_switch);
        plySwitch = viewVar.findViewById(R.id.ply_switch);

        genderSpinner = viewVar.findViewById(R.id.gender_spinner);
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
                    if(!TextUtils.isEmpty(editable)) {
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
            floorEntry.setText(bathroom.getFloor().toString());
            floorEntry.setEnabled(false);

            // Set room number, disable input
            roomEntry.setText(bathroom.getRmNum().toString());
            roomEntry.setEnabled(false);

            // Set gender, disable input
            genderSpinner.setSelection(genderAdapter.getPosition(bathroom.getGender()));
            genderSpinner.setEnabled(false);

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

        FloatingActionButton fab = viewVar.findViewById(R.id.sendButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReview(building, bathroom);
            }
        });

        return viewVar;
    }

    private void attemptReview(Building building, Bathroom bathroom) {
        Map<String, String> queries = new HashMap<>();
        boolean reviewEmpty = TextUtils.isEmpty(detailsEntry.getText());

        if (cleanRatingEmpty) {
            Snackbar.make(viewVar, "Please rate how clean this bathroom is.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (smellRatingEmpty) {
            Snackbar.make(viewVar, "Please rate how well this bathroom smells.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quietRatingEmpty) {
            Snackbar.make(viewVar, "Please rate how quiet, or empty, this bathroom typically is.", Toast.LENGTH_SHORT).show();
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
                    enqueue(new AddBathroomCallback(getContext(), viewVar, !reviewEmpty));
        } else {
            queries.put("cleanRating", Integer.toString(cleanStars));
            queries.put("emptyRating", Integer.toString(quietStars));
            queries.put("smellRating", Integer.toString(smellStars));
            queries.put("handicap", Boolean.toString(handicap));
            queries.put("plyCount", Integer.toString(ply));
            service.updateBathroom(building.getId(), bathroom.getId(), queries, sharedPrefs.getString("jwt", "")).enqueue(new AddBathroomCallback(getContext(), viewVar, !reviewEmpty));
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

                if(hasReview) {
                    service.addReview(sharedPrefs.getInt("id", 0), b.getId(), detailsEntry.getText().toString(), sharedPrefs.getString("jwt", ""))
                            .enqueue(new AddReviewCallback(getContext(), viewVar));
                }

                Snackbar.make(viewVar, "Review sent!", Snackbar.LENGTH_LONG).show();
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
                buildingAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, buildingList);
                buildingSpn.setAdapter(buildingAdapter);

                // Set listeners for spinner, or disable if not needed
                if (bathroomExists) {
                    // Set building selection, disable input
                    buildingSpn.setSelection(buildingAdapter.getPosition(bathroom.getBuilding()));
                    buildingSpn.setEnabled(false);
                    building = buildingList.get(buildingAdapter.getPosition(bathroom.getBuilding()));
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
