package com.cse5236.bowlbuddy;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * This is the {@link Fragment} that hosts the login form.
 */
public class LoginFragment extends Fragment {
    private final static String TAG = LoginFragment.class.getSimpleName();

    private View view;
    private Button loginButton;
    private Button backButton;
    private EditText usernameField;
    private EditText passwordField;
    private String userName;
    private String password;
    private APIService service;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);

        // Populate UI elements
        loginButton = view.findViewById(R.id.loginButton);
        backButton = view.findViewById(R.id.backButton);
        usernameField = view.findViewById(R.id.usernameField);
        passwordField = view.findViewById(R.id.passwordField);

        // Initialize service variable that is used to communicate with the server
        service = APISingleton.getInstance();

        if (loginButton != null) {
            // Set the listener for the login button
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Get the username and passwords for the user
                    userName = usernameField.getText().toString();
                    password = passwordField.getText().toString();

                    // Check credentials to see if they match the server
                    service.login(userName, password).enqueue(new LoginCallback(getContext(), view));
                }
            });
        }

        if (backButton != null) {
            // Set the listener for the back button
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Go back a fragment if the back button is pressed
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStackImmediate();
                }
            });
        }

        Log.d(TAG, "onCreateView: View successfully created");
        return view;
    }

    /**
     * Method used to start the master list activity
     */
    private void startNextActivity() {
        // Start the MasterListActivity
        Intent intent = new Intent(getActivity(), MasterListActivity.class);
        startActivity(intent);

        // Finish current activity, so that the user cannot "back" into it.
        getActivity().finish();
    }

    private class LoginCallback extends BowlBuddyCallback<ResponseBody> {
        public LoginCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                // Check if the response was successful
                if (response.isSuccessful()) {
                    // Get a json object of the login info
                    JSONObject json = new JSONObject(response.body().string());

                    // Parse the authentication token for the user
                    String jwt = json.getString("jwt");
                    String username = json.getString("username");

                    // Get the user ide
                    int id = json.getInt("id");

                    // Store user info to "Session" SharedPreferences
                    SharedPreferences sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);
                    sharedPrefs.edit().putString("jwt", jwt).apply();
                    sharedPrefs.edit().putString("username", username).apply();
                    sharedPrefs.edit().putInt("id", id).apply();

                    // Start MasterListActivity
                    startNextActivity();
                } else {
                    // Print error if present
                    parseError(response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
