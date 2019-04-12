package com.cse5236.bowlbuddy;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = HomePageFragment.class.getSimpleName();

    public HomePageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        // Get instances for the home page buttons
        Button loginButton = view.findViewById(R.id.loginButton);
        Button signUpButton = view.findViewById(R.id.signUpButton);

        // Set this fragment to be the listener for the sign up and login buttons
        if (loginButton != null) {
            loginButton.setOnClickListener(this);
        }
        if (signUpButton != null) {
            signUpButton.setOnClickListener(this);
        }

        Log.d(TAG, "onCreateView: View successfully created");
        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.loginButton:
                startLogin();
                break;
            case R.id.signUpButton:
                startSignUp();
                break;
            default:
                Snackbar.make(view, "unrecognized button id", Snackbar.LENGTH_SHORT).show();
        }


    }

    /**
     * Method used to handle when a user wants to login
     */
    private void startLogin() {
        // Get an instance of the login fragment
        Fragment loginFragment = new LoginFragment();
        FragmentManager manager = getFragmentManager();

        // Start up the login fragment
        manager.beginTransaction()
                .replace(R.id.loadingScreen, loginFragment, loginFragment.getTag())
                .addToBackStack(null)  // Add to Fragments back stack for easy back navigation
                .commit();
    }

    /**
     * Method used to handle when a user wants to sign up
     */
    private void startSignUp() {
        // Get an instance of the sigh up fragment
        Fragment signUpFragment = new SignUpFragment();
        FragmentManager manager = getFragmentManager();

        // Start up the sign up fragment
        manager.beginTransaction()
                .replace(R.id.loadingScreen, signUpFragment, signUpFragment.getTag())
                .addToBackStack(null)
                .commit();
    }

}
