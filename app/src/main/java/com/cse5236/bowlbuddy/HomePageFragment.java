package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener {

    private View viewVar;

    public HomePageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewVar = inflater.inflate(R.layout.home_page_fragment, container, false);

        Button loginButton = viewVar.findViewById(R.id.loginButton);
        Button signUpButton = viewVar.findViewById(R.id.signUpButton);

        if (loginButton != null) {
            loginButton.setOnClickListener(this);
        }
        if (signUpButton != null) {
            signUpButton.setOnClickListener(this);
        }

        return viewVar;
    }

    @Override
    public void onClick(View view) {

        Activity activity = getActivity();

            switch (view.getId()) {
                case R.id.loginButton:
                    startLogin();
                    break;
                case R.id.signUpButton:
                    startSignUp();
                    break;
                default:
                    Toast.makeText(activity, "unrecognized button id", Toast.LENGTH_SHORT).show();
            }


    }

    private void startLogin() {
        Fragment loginFragment = new LoginFragment();
        FragmentManager manager = getFragmentManager();

        manager.beginTransaction()
                .replace(R.id.loadingScreen, loginFragment, loginFragment.getTag())
                .addToBackStack(null)  // Add to Fragments back stack for easy back navigation
                .commit();
    }

     private void startSignUp() {
        Fragment signUpFragment = new SignUpFragment();
        FragmentManager manager = getFragmentManager();

        manager.beginTransaction()
                .replace(R.id.loadingScreen, signUpFragment, signUpFragment.getTag())
                .addToBackStack(null)
                .commit();
     }

}
