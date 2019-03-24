package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment containing a the details about a specific bathroom
 */
public class DetailsActivityFragment extends android.support.v4.app.Fragment {

    TextView genderField;

    // TODO: Programmatically request image urls from webserver
    private String[] imageUrls = new String[] {
            "https://st.hzcdn.com/simgs/c881faaa0672e118_4-2734/traditional-bathroom.jpg",
            "https://93fvk2j4yjt36cujr3idg8f1-wpengine.netdna-ssl.com/wp-content/uploads/2017/03/cindy-bathroom-1.jpg",
            "https://hgtvhome.sndimg.com/content/dam/images/hgtv/fullset/2009/11/16/1/HDIVD1510_master-bathroom-after_s4x3.jpg.rend.hgtvcom.1280.960.suffix/1400949240724.jpeg"
    };

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), imageUrls);
        viewPager.setAdapter(adapter);
        genderField = view.findViewById(R.id.genderField);
        DetailsActivity activity = (DetailsActivity) getActivity();

        setGender(activity.getIntent().getStringExtra("gender"));
        return view;
    }

    public void setGender(String gender) {
        genderField.setText(gender);
    }

}
