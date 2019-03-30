package com.cse5236.bowlbuddy;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
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
    private View view;
    private FloatingActionButton rootFAB;
    private FloatingActionButton addFAB;
    private FloatingActionButton favoriteFAB;
    private Bathroom bathroom;
    private SharedPreferences sharedPrefs;
    private RecyclerView reviewRecyclerView;
    private RecyclerView.Adapter reviewAdapter;
    private RecyclerView.LayoutManager reviewLayoutManager;
    private List<Review> reviewList;
    private ArrayList<Bathroom> favoritesList;
    private boolean isFavorited;
    private RatingBar ratingBar;


    // TODO: Programmatically request image urls from webserver
    private String[] imageUrls = new String[]{
            "https://st.hzcdn.com/simgs/c881faaa0672e118_4-2734/traditional-bathroom.jpg",
            "https://93fvk2j4yjt36cujr3idg8f1-wpengine.netdna-ssl.com/wp-content/uploads/2017/03/cindy-bathroom-1.jpg",
            "https://hgtvhome.sndimg.com/content/dam/images/hgtv/fullset/2009/11/16/1/HDIVD1510_master-bathroom-after_s4x3.jpg.rend.hgtvcom.1280.960.suffix/1400949240724.jpeg"
    };

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_details, container, false);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), imageUrls);
        viewPager.setAdapter(adapter);
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

        setStars(bathroom.getAverageRating());
        setRoom(bathroom.getRmNum());
        setFloor(bathroom.getFloor());
        setGender(bathroom.getGender());
        setHandicap(bathroom.isHandicap());
        setTitle(bathroom.getBuilding().getName());

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

        rootFAB = view.findViewById(R.id.details_root_fab);
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
        service.getBathroomReviews(bathroom.getId(), sharedPrefs.getString("jwt", ""))
                .enqueue(new ReviewListCallback(getContext(), view));

        reviewAdapter = new ReviewAdapter();
        reviewLayoutManager = new LinearLayoutManager(activity);
        reviewRecyclerView = view.findViewById(R.id.review_recycler_view);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewRecyclerView.setAdapter(reviewAdapter);

        return view;
    }

    public void setGender(String gender) {
        genderField.setText(gender);
    }

    public void setHandicap(Boolean handicap) {
        String access = "Handicap Accessible";
        String no_access = "Not Accessible";
        if (handicap) {
            handicapField.setText(access);
        } else {
            handicapField.setText(no_access);
        }
    }

    public void setStars(Float rating) {
        ratingBar.setRating(rating);
    }

    public void setTitle(String title) {
        titleField.setText(title);
    }

    public void setFloor(Integer floor) { floorField.setText(floor.toString());}

    public void setRoom(Integer room) { roomField.setText(room.toString()); }

    private class ReviewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private TextView details;

        public ReviewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_review, parent, false));

            username = itemView.findViewById(R.id.review_username);
            details = itemView.findViewById(R.id.review_details);
        }

        public void bind(Review review) {
            username.setText(review.getAuthorName());
            details.setText(review.getDetails());
        }
    }

    public void returnResult() {
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
        public ReviewListCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
            if (response.isSuccessful()) {
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
        public AddFavoriteCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
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
        public DeleteFavoriteCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<List<Bathroom>> call, Response<List<Bathroom>> response) {
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
