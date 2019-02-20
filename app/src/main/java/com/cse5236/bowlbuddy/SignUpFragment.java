package com.cse5236.bowlbuddy;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {
    private final static String TAG = SignUpFragment.class.getSimpleName();

    private View viewVar;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewVar = inflater.inflate(R.layout.sign_up_fragment, container, false);

        Log.d(TAG, "onCreateView: View successfully created");
        return viewVar;
    }

}
