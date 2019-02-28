package com.cse5236.bowlbuddy;


import android.app.Activity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener, Callback<User> {
    private final static String TAG = SignUpFragment.class.getSimpleName();

    private View viewVar;
    private Button signUpButton;
    private Button backButton;
    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private String userName;
    private String password;
    private String confirmPassword;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewVar = inflater.inflate(R.layout.sign_up_fragment, container, false);

        signUpButton = viewVar.findViewById(R.id.signUpButton);
        backButton = viewVar.findViewById(R.id.backButton);
        usernameField = viewVar.findViewById(R.id.usernameField);
        passwordField = viewVar.findViewById(R.id.passwordField);
        confirmPasswordField = viewVar.findViewById(R.id.confirmPasswordField);

        if (signUpButton != null) {
            signUpButton.setOnClickListener(this);
        }
        if (backButton != null) {
            backButton.setOnClickListener(this);
        }

        Log.d(TAG, "onCreateView: View successfully created");
        return viewVar;
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

    private void startSignUp() {
        LoadingScreenActivity activity = (LoadingScreenActivity) getActivity();
        userName = usernameField.getText().toString();
        password = passwordField.getText().toString();
        confirmPassword = confirmPasswordField.getText().toString();

        // Check that the password and confirm password are the same.
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Password and Confirmation Password did not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that the username does not contain spaces.
        if (!userName.matches("[A-Za-z0-9_]+")) {
            Snackbar.make(getView(), "Username must only contain letters, digits, or underscores.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        activity.getService().signUp(userName, password, confirmPassword).enqueue(this);
    }

    private void goBack() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onResponse(Call<User> call, Response<User> response) {
        try {
            if (response.isSuccessful()) {
                User u = response.body();
                Log.d(TAG, "onResponse: Received successful signup response.");
                Snackbar.make(getView(), "Account created!", Snackbar.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
            } else {
                JSONObject json = new JSONObject(response.errorBody().string());
                Snackbar.make(getView(), json.getString("error"), Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "onResponse: Unsuccessful signup response, status " + response.code());
            }
        } catch (JSONException e) {
            Log.e(TAG, "onResponse: Error parsing JSON", e);
        } catch (IOException e) {
            Log.e(TAG, "onResponse: Error getting errorBody string", e);
        }
    }

    @Override
    public void onFailure(Call<User> call, Throwable t) {
        LoadingScreenActivity activity = (LoadingScreenActivity) getActivity();
        Toast.makeText(activity, "Network error", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onFailure: Signup call failed", t);
    }
}
