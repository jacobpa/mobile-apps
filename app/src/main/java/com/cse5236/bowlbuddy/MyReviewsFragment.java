package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cse5236.bowlbuddy.models.Review;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.List;

import retrofit2.Call;
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

        public ReviewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.my_reviews_list, parent, false));

            itemView.setOnClickListener(this);

            bathroomName = itemView.findViewById(R.id.bathroom_name);
            reviewDetails = itemView.findViewById(R.id.bathroom_desc);
        }

        public void bind(Review review) {
            this.review = review;

            if (review != null){
                String details = review.getReviewText();
                reviewDetails.setText(details);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.delete_review_btn:
                    // do delete stuff
                    break;
                case R.id.edit_review_btn:
                    // do edit stuff
                    break;
            }
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
        public GetReviewsCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
            if (response.isSuccessful()) {
                reviewList = response.body();
                reviewsAdapter.notifyDataSetChanged();;
                Log.d(TAG, "onResponse: Response is " + reviewList);
            } else {
                parseError(response);
            }
        }

    }

}
