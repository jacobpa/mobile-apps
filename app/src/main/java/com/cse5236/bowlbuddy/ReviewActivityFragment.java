package com.cse5236.bowlbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.List;

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

    private AutoCompleteTextView entry;
    private AutoCompleteTextView roomEntry;

    private boolean handicap;
    private boolean ply;
    String gender;

    List<Building> buildingList;
    Building building;
    Bathroom bathroom;
    private Spinner buildingSpn;
    private Spinner floorSpn;

    int smellStars;
    int quietStars;
    int cleanStars;
    int floor;

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewVar = inflater.inflate(R.layout.fragment_review, container, false);

        service = APISingleton.getInstance();
        Intent intent = getActivity().getIntent();
        final SharedPreferences sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        floorSpn = viewVar.findViewById(R.id.floor_spinner);
        buildingSpn = viewVar.findViewById(R.id.building_spinner);
        roomEntry = viewVar.findViewById(R.id.room_entry);
        if(intent.getStringExtra("caller").equals("MasterListFragment")) {
            service.getAllBuildings(sharedPrefs.getString("jwt", "")).enqueue(new GetBuildingsCallback(getContext(),viewVar));
            Integer[] floors = new Integer[]{1,2,3,4,5,6,7,8,9,10};
            ArrayAdapter<Integer> floorAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, floors);
            floorSpn.setAdapter(floorAdapter);
            floorSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    floor = position + 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // left blank
                }
            });
        }
        else {
            TextView floorField = viewVar.findViewById(R.id.floor_field);
            TextView titleField = viewVar.findViewById(R.id.building_field);
            TextView roomField = viewVar.findViewById(R.id.room_field);

            floorField.setVisibility(View.INVISIBLE);
            titleField.setVisibility(View.INVISIBLE);
            buildingSpn.setVisibility(View.INVISIBLE);
            floorSpn.setVisibility(View.INVISIBLE);
            roomEntry.setVisibility(View.INVISIBLE);
            roomField.setVisibility(View.INVISIBLE);
            TextView title = viewVar.findViewById(R.id.title_field);
            TextView floor = viewVar.findViewById(R.id.floor_header);
            Bundle bundle = intent.getExtras();
            bathroom = (Bathroom) bundle.getSerializable("bathroom");
            title.setText(bathroom.getBuilding().toString());
            floor.setText(bathroom.getFloor().toString());
        }

        FloatingActionButton fab = viewVar.findViewById(R.id.sendButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(getActivity().getIntent().getStringExtra("caller").equals("MasterListFragment")) {
                    int room = Integer.parseInt(roomEntry.getText().toString());
                    service.addBathroom(building.getId(), floor, room, gender, cleanStars, quietStars, smellStars, sharedPrefs.getString("jwt", "")).enqueue(new AddBathroomCallback(getContext(), viewVar));
                    //service.addReview(sharedPrefs.getInt("id", 0), bathroomId, entry.getText().toString(), sharedPrefs.getString("jwt", "")).enqueue(new AddReviewCallback(getContext(), viewVar));
                }
                else {
                    service.addReview(sharedPrefs.getInt("id", 0), bathroom.getId(), entry.getText().toString(), sharedPrefs.getString("jwt", "")).enqueue(new AddReviewCallback(getContext(), viewVar));
                }
                Toast.makeText(getActivity(), "Review Sent", Toast.LENGTH_SHORT).show();
            }
        });

        entry = viewVar.findViewById(R.id.review_field);
        RatingBar smellBar = viewVar.findViewById(R.id.smellRating);
        RatingBar quietBar = viewVar.findViewById(R.id.cleanRating);
        RatingBar cleanBar = viewVar.findViewById(R.id.quietRating);
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

        handicapBtn = viewVar.findViewById(R.id.handicapButton);
        genderBtn = viewVar.findViewById(R.id.genderButton);
        tpBtn = viewVar.findViewById(R.id.plyButton);
        gender = "Male";
        genderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gender.equals("Male")) {
                    genderBtn.setBackgroundColor(Color.parseColor("#ff91f5"));
                    gender = "Female";
                }
                else {
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
                    handicapBtn.setBackgroundColor(Color.parseColor(""));
                    handicap = false;
                }
                else {
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
                }
                else {
                    ply = true;
                    tpBtn.setBackgroundColor(Color.parseColor("#4286f4"));
                }
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
            if (response.isSuccessful()) { Log.d(TAG, "onResponse: Response is " + response); }
            else { parseError(response); }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            super.onFailure(call, t);
        }
    }

    private class AddBathroomCallback extends BowlBuddyCallback<Void> {
        public AddBathroomCallback(Context context, View view) {
            super(context, view);
        }
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) { Log.d(TAG, "buffalo onResponse: Response is " + response); }
            else { parseError(response); }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            super.onFailure(call, t);
        }
    }

    private class GetBuildingsCallback extends BowlBuddyCallback<List<Building>> {
        public GetBuildingsCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Building>> call, Response<List<Building>> response) {
            if (response.isSuccessful()) {
                buildingList = response.body();

                Log.d(TAG, "onResponse: Response is " + buildingList);

                ArrayAdapter<Building> adapter = new ArrayAdapter<Building>(getActivity(),
                        android.R.layout.simple_spinner_item, buildingList);
                buildingSpn.setAdapter(adapter);
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
