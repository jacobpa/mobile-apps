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
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;

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
    private SharedPreferences sharedPreferences;
    private View viewVar;

    private Button genderBtn;
    private Button handicapBtn;
    private Button tpBtn;

    private boolean handicap;
    private boolean ply;
    String gender;

    List<Building> buildingList;
    private Spinner buildingSpn;
    private Spinner floorSpn;

    int smellStars;
    int quietStars;
    int cleanStars;

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewVar = inflater.inflate(R.layout.fragment_review, container, false);

        service = APISingleton.getInstance();
        Intent intent = getActivity().getIntent();
        SharedPreferences sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        service.getAllBuildings(sharedPrefs.getString("jwt", "")).enqueue(new GetBuildingsCallback(getContext(),viewVar));

        FloatingActionButton fab = (FloatingActionButton) viewVar.findViewById(R.id.sendButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Post Review Details
            }
        });

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
        gender = "male";
        genderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gender.equals("male")) {
                    genderBtn.setBackgroundColor(Color.parseColor("#4286f4"));
                    gender = "female";
                }
                else {
                    genderBtn.setBackgroundColor(Color.parseColor("#ff91f5"));
                    gender = "male";
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

    private class GetBuildingsCallback extends BowlBuddyCallback<List<Building>> {
        public GetBuildingsCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Building>> call, Response<List<Building>> response) {
            if (response.isSuccessful()) {
                buildingList = response.body();

                Log.d(TAG, "onResponse: Response is " + buildingList);
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
