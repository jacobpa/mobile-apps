package com.cse5236.bowlbuddy;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment containing a the details about a specific bathroom
 */
public class DetailsActivityFragment extends Fragment {

    private String[] imageUrls = new String[] {
            "https://via.placeholder.com/150/0C0BFF/808080?Text=test1",
            "https://via.placeholder.com/200/BBBBBF/808080?Text=test2",
            "https://via.placeholder.com/100/0000AF/808080?Text=test3"
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

        return view;
    }
}
