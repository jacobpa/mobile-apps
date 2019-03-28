package com.cse5236.bowlbuddy;

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
import com.cse5236.bowlbuddy.models.User;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A fragment containing a the details about a specific bathroom
 */
public class DetailsActivityFragment extends android.support.v4.app.Fragment {
    private static final String TAG = DetailsActivityFragment.class.getSimpleName();
    TextView genderField;
    TextView handicapField;
    TextView titleField;
    TextView noReviewMessage;
    RatingBar ratingBar;
    APIService service;
    View view;
    private Bathroom bathroom;
    private SharedPreferences sharedPrefs;
    private RecyclerView reviewRecyclerView;
    private RecyclerView.Adapter reviewAdapter;
    private RecyclerView.LayoutManager reviewLayoutManager;
    private List<Review> reviewList;


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
        handicapField = view.findViewById(R.id.handicapField);
        titleField = view.findViewById(R.id.titleField);
        noReviewMessage = view.findViewById(R.id.no_reviews_message);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);

        DetailsActivity activity = (DetailsActivity) getActivity();

        bathroom = (Bathroom) activity.getIntent().getExtras().getSerializable("bathroom");

        setGender(bathroom.getGender());
        setHandicap(bathroom.isHandicap());
        setTitle(bathroom.getBuilding().getName());

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if(fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
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
        if (handicap != null && handicap) {
            handicapField.setText(access);
        } else {
            handicapField.setText(no_access);
        }
    }

    public void setTitle(String title) {
        titleField.setText(title);
    }

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
}
