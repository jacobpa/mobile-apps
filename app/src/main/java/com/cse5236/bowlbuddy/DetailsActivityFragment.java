package com.cse5236.bowlbuddy;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Review;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A fragment containing a the details about a specific bathroom
 */
public class DetailsActivityFragment extends android.support.v4.app.Fragment {
    private static final String TAG = DetailsActivityFragment.class.getSimpleName();
    private TextView genderField;
    private TextView handicapField;
    private TextView titleField;
    private TextView roomField;
    private TextView noReviewMessage;
    private TextView floorField;
    private APIService service;
    private FloatingActionButton addFAB;
    private FloatingActionButton favoriteFAB;
    private Bathroom bathroom;
    private SharedPreferences sharedPrefs;
    private RecyclerView reviewRecyclerView;
    private RecyclerView.Adapter reviewAdapter;
    private List<Review> reviewList;
    private ArrayList<Bathroom> favoritesList;
    private boolean isFavorited;
    private RatingBar ratingBar;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);
        genderField = view.findViewById(R.id.genderField);
        floorField = view.findViewById(R.id.floor_field_desc);
        handicapField = view.findViewById(R.id.handicapField);
        titleField = view.findViewById(R.id.titleField);
        roomField = view.findViewById(R.id.room_field_desc);
        noReviewMessage = view.findViewById(R.id.no_reviews_message);
        ratingBar = view.findViewById(R.id.ratingBar);

        DetailsActivity activity = (DetailsActivity) getActivity();
        sharedPrefs = activity.getSharedPreferences("Session", Context.MODE_PRIVATE);

        bathroom = (Bathroom) activity.getIntent().getExtras().getSerializable("bathroom");
        favoritesList = (ArrayList<Bathroom>) activity.getIntent().getExtras().getSerializable("favorites");

        if(bathroom != null) {
            setStars(bathroom.getAverageRating());
            setRoom(bathroom.getRmNum());
            setFloor(bathroom.getFloor());
            setGender(bathroom.getGender());
            setHandicap(bathroom.isHandicap());
            if(bathroom.getBuilding() != null) {
                setTitle(bathroom.getBuilding().getName());
            }
        }

        addFAB = view.findViewById(R.id.add_review_fab);
        if(addFAB != null) {
            addFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ReviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bathroom", bathroom);
                    bundle.putString("caller", "DetailsActivityFragment");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        favoriteFAB = view.findViewById(R.id.favorite_fab);
        if (favoriteFAB != null) {
            if (favoritesList.contains(bathroom)) {
                favoriteFAB.setImageResource(R.drawable.ic_favorite_white);
                isFavorited = true;
            }

            favoriteFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isFavorited) {
                        service.deleteFavorite(sharedPrefs.getInt("id", 0), bathroom.getId(), sharedPrefs.getString("jwt", ""))
                                .enqueue(new DeleteFavoriteCallback(getContext(), view));
                    } else {
                        service.addFavorite(sharedPrefs.getInt("id", 0), bathroom.getId(), sharedPrefs.getString("jwt", ""))
                                .enqueue(new AddFavoriteCallback(getContext(), view));
                    }
                }
            });
        }

        FloatingActionButton rootFAB = view.findViewById(R.id.details_root_fab);
        if (rootFAB != null) {
            rootFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (addFAB.getVisibility() == View.GONE) {
                        addFAB.setVisibility(View.VISIBLE);
                        favoriteFAB.setVisibility(View.VISIBLE);
                    } else {
                        addFAB.setVisibility(View.GONE);
                        favoriteFAB.setVisibility(View.GONE);
                    }
                }
            });
        }
        sharedPrefs = activity.getSharedPreferences("Session", Context.MODE_PRIVATE);

        service = APISingleton.getInstance();

        if(bathroom.getId() != null) {
            service.getBathroomReviews(bathroom.getId(), sharedPrefs.getString("jwt", ""))
                    .enqueue(new ReviewListCallback(getContext(), view));
        }

        reviewAdapter = new ReviewAdapter();
        RecyclerView.LayoutManager reviewLayoutManager = new LinearLayoutManager(activity);
        reviewRecyclerView = view.findViewById(R.id.review_recycler_view);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewRecyclerView.setAdapter(reviewAdapter);

        return view;
    }

    private void setGender(String gender) {
        if(gender != null) {
            genderField.setText(gender);
        }
    }

    private void setHandicap(Boolean handicap) {
        if(handicap != null) {
            String access = "Handicap Accessible";
            String no_access = "Not Accessible";
            if (handicap) {
                handicapField.setText(access);
            } else {
                handicapField.setText(no_access);
            }
        }
    }

    private void setStars(Float rating) {
        if(rating != null) {
            ratingBar.setRating(rating);
        }
    }

    private void setTitle(String title) {
        if(title != null) {
            titleField.setText(title);
        }
    }

    private void setFloor(Integer floor) {
        if(floor != null) {
            floorField.setText(String.format(Locale.getDefault(), "%d", floor));
        }
    }

    private void setRoom(Integer room) {
        if(room != null) {
            roomField.setText(String.format(Locale.getDefault(), "%d", room));
        }
    }

    private class ReviewHolder extends RecyclerView.ViewHolder {
        private final TextView username;
        private final TextView details;

        ReviewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_review, parent, false));

            username = itemView.findViewById(R.id.review_username);
            details = itemView.findViewById(R.id.review_details);
        }

        void bind(Review review) {
            username.setText(review.getAuthorName());
            details.setText(review.getDetails());
        }
    }

    private void returnResult() {
        Intent data = new Intent();
        data.putExtra("favorites", favoritesList);
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewHolder> {

        @NonNull
        @Override
        public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ReviewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
            Review review = reviewList.get(position);
            holder.bind(review);
        }

        @Override
        public int getItemCount() {
            if (reviewList == null)
                return 0;
            return reviewList.size();
        }
    }

    private class ReviewListCallback extends BowlBuddyCallback<List<Review>> {
        ReviewListCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
            if (response.isSuccessful() && response.body() != null) {
                reviewList = response.body();
                Log.d(TAG, "onResponse: Got list of reviews with length " + reviewList.size());

                if (reviewList.size() == 0) {
                    reviewRecyclerView.setVisibility(View.GONE);
                    noReviewMessage.setVisibility(View.VISIBLE);
                }
                reviewAdapter.notifyDataSetChanged();
            } else {
                parseError(response);
            }
        }
    }

    private class AddFavoriteCallback extends BowlBuddyCallback<List<Bathroom>> {
        AddFavoriteCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<List<Bathroom>> call, @NonNull Response<List<Bathroom>> response) {
            if (response.isSuccessful()) {
                favoriteFAB.setImageResource(R.drawable.ic_favorite_white);
                favoritesList.clear();
                favoritesList.addAll(response.body());
                isFavorited = true;
                returnResult();
            } else {
                parseError(response);
            }
        }
    }

    private class DeleteFavoriteCallback extends BowlBuddyCallback<List<Bathroom>> {
        DeleteFavoriteCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<List<Bathroom>> call, @NonNull Response<List<Bathroom>> response) {
            if (response.isSuccessful()) {
                favoriteFAB.setImageResource(R.drawable.ic_favorite_border_white);
                favoritesList.clear();
                favoritesList.addAll(response.body());
                isFavorited = false;
                returnResult();
            } else {
                parseError(response);
            }
        }
    }
}
