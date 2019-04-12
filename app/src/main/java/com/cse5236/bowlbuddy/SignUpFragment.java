package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cse5236.bowlbuddy.models.User;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import retrofit2.Call;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = SignUpFragment.class.getSimpleName();

    private View view;
    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Get instances for the buttons and user information
        Button signUpButton = view.findViewById(R.id.signUpButton);
        Button backButton = view.findViewById(R.id.backButton);
        usernameField = view.findViewById(R.id.usernameField);
        passwordField = view.findViewById(R.id.passwordField);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordField);

        // Set the button listeners to this fragment
        if (signUpButton != null) {
            signUpButton.setOnClickListener(this);
        }
        if (backButton != null) {
            backButton.setOnClickListener(this);
        }

        Log.d(TAG, "onCreateView: View successfully created");
        return view;
    }


    @Override
    public void onClick(View view) {
        Activity activity = getActivity();

        switch (view.getId()) {
            case R.id.signUpButton:
                startSignUp();
                break;
            case R.id.backButton:
                goBack();
                break;
            default:
                Toast.makeText(activity, "unrecognized button id", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method used to by a user to sign up with a new account
     */
    private void startSignUp() {
        LoadingScreenActivity activity = (LoadingScreenActivity) getActivity();

        // Get the user input username, password, and conformation password
        String userName = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        // Check that the password and confirm password are the same.
        boolean passwordsMatch = password.equals(confirmPassword);
        boolean fieldsEmpty = password.isEmpty() || confirmPassword.isEmpty();

        if (!passwordsMatch) {
            Snackbar.make(view, "Password and Confirmation Password did not match.", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        if (fieldsEmpty) {
            Snackbar.make(view, "Password fields can not be empty.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Check that the username does not contain spaces.
        if (!User.isUsernameValid(userName)) {
            Snackbar.make(getView(), "Username must only contain letters, digits, or underscores.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        activity.getService().signUp(userName, password, confirmPassword).enqueue(new SignUpCallback(getContext(), view));
    }

    private void goBack() {
        getFragmentManager().popBackStack();
    }

    private class SignUpCallback extends BowlBuddyCallback<User> {
        SignUpCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<User> call, Response<User> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "onResponse: Received successful signup response.");
                Snackbar.make(getView(), "Account created!", Snackbar.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
            } else {
                parseError(response);
            }
        }
    }
}
