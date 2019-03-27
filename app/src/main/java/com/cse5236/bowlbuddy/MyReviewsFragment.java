package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;
import com.cse5236.bowlbuddy.models.Review;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReviewsFragment extends Fragment {
    private static final String TAG = MyReviewsFragment.class.getSimpleName();
    private View view;
    private RecyclerView reviewsRecyclerView;
    private RecyclerView.Adapter reviewsAdapter;
    private RecyclerView.LayoutManager reviewsLayoutManager;
    private List<Review> reviewList;
    private APIService service;
    private SharedPreferences sharedPreferences;

    public MyReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_reviews, container, false);
        Activity activity = getActivity();

        service = APISingleton.getInstance();
        sharedPreferences = activity.getSharedPreferences("Session", Context.MODE_PRIVATE);

        reviewsAdapter = new ReviewsAdapter();
        reviewsLayoutManager = new LinearLayoutManager(activity);
        reviewsRecyclerView = view.findViewById(R.id.recycler_view);
        reviewsRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setLayoutManager(reviewsLayoutManager);
        reviewsRecyclerView.setAdapter(reviewsAdapter);

        service.getUserReviews(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetReviewsCallback(getContext(), view));

        Log.d(TAG, "onCreateView: View successfully created");
        // Inflate the layout for this fragment
        return view;

    }

    private class ReviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView bathroomName;
        private TextView reviewDetails;
        private Review review;
        private Bathroom bathroom;
        private Building building;
        private EditText editDetails;
        private LinearLayout editDetailsLayout;
        private LinearLayout buttonLayout;

        public ReviewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.my_reviews_list, parent, false));

            itemView.setOnClickListener(this);

            bathroomName = itemView.findViewById(R.id.bathroom_name);
            reviewDetails = itemView.findViewById(R.id.bathroom_desc);
            editDetails = itemView.findViewById(R.id.edit_details);
            editDetailsLayout = itemView.findViewById(R.id.edit_details_layout);
            buttonLayout = itemView.findViewById(R.id.button_layout);

            itemView.findViewById(R.id.delete_review_btn).setOnClickListener(this);
            itemView.findViewById(R.id.edit_review_btn).setOnClickListener(this);
            itemView.findViewById(R.id.save_review).setOnClickListener(this);
            itemView.findViewById(R.id.cancel_review).setOnClickListener(this);
        }

        public void bind(Review review) {
            this.review = review;
            this.bathroom = review.getBathroom();
            this.building = bathroom.getBuilding();

            if (this.review != null && this.bathroom != null && this.building != null){
                String details = review.getDetails();
                String title = String.format("%s: Floor %d, Room %d",
                        building.getName(),
                        bathroom.getFloor(),
                        bathroom.getRmNum());
                reviewDetails.setText(details);
                bathroomName.setText(title);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.delete_review_btn:
                    deleteReview();
                    break;
                case R.id.edit_review_btn:
                    editReview();
                    break;
                case R.id.save_review:
                    saveReview();
                    break;
                case R.id.cancel_review:
                    cancelReview();
                    break;
                default:
                    break;
            }
        }

        public void deleteReview() {

            service.deleteReview(this.review.getUserID(), this.review.getReviewID(), sharedPreferences.getString("jwt", ""))
            .enqueue(new DeleteReviewCallback(getContext(), view));

            reviewList.remove(getAdapterPosition());
            reviewsAdapter.notifyItemRemoved(getAdapterPosition());
            reviewsAdapter.notifyItemRangeChanged(getAdapterPosition(), reviewList.size());

        }

        public void editReview() {
            editDetailsLayout.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.GONE);
        }

        public void saveReview() {
            String newReview = editDetails.getText().toString();
            if (!newReview.equals("")) {
                review.setDetails(newReview);
                service.updateUserReview(this.review.getUserID(), this.review.getReviewID(), newReview, sharedPreferences.getString("jwt", ""))
                        .enqueue(new UpdateUserReviewCallback(getContext(), view));
                reviewDetails.setText(newReview);
                editDetailsLayout.setVisibility(View.GONE);
                buttonLayout.setVisibility(View.VISIBLE);
            } else {
                Snackbar.make(view, "Please Enter Review Before Saving", Snackbar.LENGTH_LONG).show();
            }
        }

        public void cancelReview() {
            editDetailsLayout.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.VISIBLE);
        }
    }

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewHolder> {

        @NonNull
        @Override
        public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new ReviewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
            Review review = reviewList.get(position);
            holder.bind(review);
        }

        @Override
        public int getItemCount() {
            if (reviewList == null) {
                return 0;
            }
            return reviewList.size();
        }

    }

    private class GetReviewsCallback extends BowlBuddyCallback<List<Review>> {
        private Context callbackContext;

        public GetReviewsCallback(Context context, View view) {

            super(context, view);
            this.callbackContext = context;
        }

        @Override
        public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
            if (response.isSuccessful()) {
                reviewList = response.body();

                for (Review review : reviewList) {
                    service.getBathroom(review.getBathroomID(), sharedPreferences.getString("jwt", ""))
                            .enqueue(new GetBathroomCallback(this.callbackContext, view, review));
                }
                Log.d(TAG, "onResponse: Response is " + reviewList);
            } else {
                parseError(response);
            }
        }

    }

    private class GetBathroomCallback extends BowlBuddyCallback<Bathroom> {
        private Review review;
        private Context callbackContext;

        public GetBathroomCallback(Context context, View view, Review review) {
            super(context, view);
            this.review = review;
            this.callbackContext = context;
        }

        @Override
        public void onResponse(Call<Bathroom> call, Response<Bathroom> response) {
            if (response.isSuccessful()) {
                this.review.setBathroom(response.body());
                service.getLocation(this.review.getBathroom().getBuildingID(), sharedPreferences.getString("jwt", ""))
                        .enqueue(new GetBuildingCallback(this.callbackContext, view, this.review.getBathroom()));
            } else {
                parseError(response);
            }
        }

        }



    private class GetBuildingCallback extends BowlBuddyCallback<Building> {
        private Bathroom bathroom;

        public GetBuildingCallback(Context context, View view, Bathroom bathroom) {
            super(context, view);
            this.bathroom = bathroom;
        }

        @Override
        public void onResponse(Call<Building> call, Response<Building> response) {
            if (response.isSuccessful()) {
                this.bathroom.setBuilding(response.body());
                reviewsAdapter.notifyDataSetChanged();
            } else {
                parseError(response);
            }
        }
    }

    private class DeleteReviewCallback extends BowlBuddyCallback<Void> {
        public DeleteReviewCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                // Does not do anything.  Recycler view modified in the holder class.
            } else {
                parseError(response);
            }
        }
    }

    private class UpdateUserReviewCallback extends BowlBuddyCallback<Void> {
        public UpdateUserReviewCallback(Context context, View view) { super(context, view); }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                // Do nothing.  Changes made in the recycler view holder class
            } else {
                parseError(response);
            }
        }
    }



    }
