package com.cse5236.bowlbuddy;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * This is the {@link Fragment} that hosts the login form.
 */
public class LoginFragment extends Fragment {
    private final static String TAG = LoginFragment.class.getSimpleName();

    private View viewVar;
    private Button loginButton;
    private Button backButton;
    private EditText usernameField;
    private EditText passwordField;
    private String userName;
    private String password;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewVar = inflater.inflate(R.layout.login_fragment, container, false);

        // Populate UI elements
        loginButton = viewVar.findViewById(R.id.loginButton);
        backButton = viewVar.findViewById(R.id.backButton);
        usernameField = viewVar.findViewById(R.id.usernameField);
        passwordField = viewVar.findViewById(R.id.passwordField);

        if(loginButton != null) {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userName = usernameField.getText().toString();
                    password = passwordField.getText().toString();
                    Intent intent = new Intent(getActivity(), MasterListActivity.class);
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Login not implemented.", Toast.LENGTH_SHORT)
                            .show();
                    // Finish current activity, so that the user cannot "back" into it.
                    // TODO: Once user logs in, make MasterListActivity new launch activity
                    getActivity().finish();
                }
            });
        }

        if(backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStackImmediate();
                }
            });
        }

        Log.d(TAG, "onCreateView: View successfully created");
        return viewVar;
    }
}
