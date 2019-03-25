package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewActivityFragment extends Fragment {

    private View viewVar;
    private RatingBar smellBar;
    private RatingBar quietBar;
    private RatingBar cleanBar;

    int smellStars;
    int quietStars;
    int cleanStars;

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewVar = inflater.inflate(R.layout.fragment_review, container, false);
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

        return viewVar;
    }

}
