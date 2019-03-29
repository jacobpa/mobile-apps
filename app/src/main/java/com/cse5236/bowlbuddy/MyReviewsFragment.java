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

import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReviewsFragment extends Fragment {
    private static final String TAG = MyReviewsFragment.class.getSimpleName();
    private static final int UPDATE_FAVORITES_REQUEST = 1;

    private View view;
    private RecyclerView reviewsRecyclerView;
    private RecyclerView.Adapter reviewsAdapter;
    private RecyclerView.LayoutManager reviewsLayoutManager;
    private List<Review> reviewList;
    private APIService service;
    private SharedPreferences sharedPreferences;
    private List<Bathroom> favoritesList;

    public MyReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_reviews, container, false);
        Activity activity = getActivity();

        // Initialize the service variable that makes queries to the server
        service = APISingleton.getInstance();

        // Get shared user information
        sharedPreferences = activity.getSharedPreferences("Session", Context.MODE_PRIVATE);

        // Set up and initialize the recycler view that shows the user's reviews
        reviewsAdapter = new ReviewsAdapter();
        reviewsLayoutManager = new LinearLayoutManager(activity);
        reviewsRecyclerView = view.findViewById(R.id.recycler_view);
        reviewsRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setLayoutManager(reviewsLayoutManager);
        reviewsRecyclerView.setAdapter(reviewsAdapter);

        // Send a request to the server to get the user's reviews
        service.getUserReviews(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetReviewsCallback(getContext(), view));

        // Send a request to the server to get the user's favorite bathrooms
        service.getFavorites(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", ""))
                .enqueue(new GetFavoritesCallback(getContext(), view));

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

            // Initialize all of the views used in this holder
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
                    // Review Detail pressed.  Start the details activity for review.
                    openDetails();
                    break;
            }
        }

        /**
         * Method used for opening a details activity for the bathroom that was selected.
         */
        public void openDetails() {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bathroom", this.bathroom);

            while (favoritesList == null) {
                // Wait for favorites list value to be populated
            }

            bundle.putSerializable("favorites", (Serializable) favoritesList);

            // Start the details activity for the review that was selected
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, UPDATE_FAVORITES_REQUEST);
        }

        /**
         * Method used to delete a selected review
         */
        public void deleteReview() {

            // Send a request to the server to delete the selected review
            service.deleteReview(this.review.getUserID(), this.review.getReviewID(), sharedPreferences.getString("jwt", ""))
            .enqueue(new DeleteReviewCallback(getContext(), view));

            // Remove the review from the recycler view and notify view of change
            reviewList.remove(getAdapterPosition());
            reviewsAdapter.notifyItemRemoved(getAdapterPosition());
            reviewsAdapter.notifyItemRangeChanged(getAdapterPosition(), reviewList.size());

        }

        /**
         * Method used to edit the details of a review
         */
        public void editReview() {
            // Change visibility of layouts to allow user to update review
            editDetailsLayout.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.GONE);
        }

        /**
         * Method used to save the updated review to the server.
         */
        public void saveReview() {
            // Get the text for the updated review
            String newReview = editDetails.getText().toString();

            // Make sure the review has information in it
            if (!newReview.equals("")) {
                // Change the details of the review in the class
                review.setDetails(newReview);

                // Send a request to the server to update the review
                service.updateUserReview(this.review.getUserID(), this.review.getReviewID(), newReview, sharedPreferences.getString("jwt", ""))
                        .enqueue(new UpdateUserReviewCallback(getContext(), view));

                // Change the text of the review on the UI
                reviewDetails.setText(newReview);

                // Change layout visibility after changing the review has complete.
                editDetailsLayout.setVisibility(View.GONE);
                buttonLayout.setVisibility(View.VISIBLE);
            } else {
                Snackbar.make(view, "Please Enter Review Before Saving", Snackbar.LENGTH_LONG).show();
            }
        }

        /**
         * Method used to cancel the edit review process.
         */
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
            // Check if response was successful
            if (response.isSuccessful()) {
                // Update the review list
                reviewList = response.body();

                // Get bathrooms for all of the reviews
                for (Review review : reviewList) {
                    service.getBathroom(review.getBathroomID(), sharedPreferences.getString("jwt", ""))
                            .enqueue(new GetBathroomCallback(this.callbackContext, view, review));
                }
                Log.d(TAG, "onResponse: Response is " + reviewList);
            } else {
                // Print error if present
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
            // Check if response was successful
            if (response.isSuccessful()) {
                // Update the review's bathroom
                this.review.setBathroom(response.body());

                // Get the building for the bathroom
                service.getLocation(this.review.getBathroom().getBuildingID(), sharedPreferences.getString("jwt", ""))
                        .enqueue(new GetBuildingCallback(this.callbackContext, view, this.review.getBathroom()));
            } else {
                // Print error if present
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
            // Check if response was successful
            if (response.isSuccessful()) {
                // Update the building for the bathroom
                this.bathroom.setBuilding(response.body());

                // Notify the recycler view of item changes
                reviewsAdapter.notifyDataSetChanged();
            } else {
                // Print error if present
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

    private class GetFavoritesCallback extends BowlBuddyCallback<List<Bathroom>> {
        public GetFavoritesCallback(Context context, View view) { super(context, view); }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
            // Check if response was successful
            if(response.isSuccessful()) {
                // Update the favorites list
                favoritesList = response.body();
            } else {
                // Print error if present
                parseError(response);
            }
        }
    }
}
