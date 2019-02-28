package com.cse5236.bowlbuddy;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This is the {@link Fragment} that hosts the login form.
 */
public class LoginFragment extends Fragment implements Callback<ResponseBody> {
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
                    LoadingScreenActivity activity = (LoadingScreenActivity) getActivity();
                    userName = usernameField.getText().toString();
                    password = passwordField.getText().toString();

                    activity.getService().login(userName, password).enqueue(LoginFragment.this);
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

    private void startNextActivity() {
        Intent intent = new Intent(getActivity(), MasterListActivity.class);
        startActivity(intent);
        // Finish current activity, so that the user cannot "back" into it.
        // TODO: Once user logs in, make MasterListActivity new launch activity
        getActivity().finish();
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        JSONObject json;
        try {
            if(response.isSuccessful()) {
                json = new JSONObject(response.body().string());

                String jwt = json.getString("jwt");
                String username = json.getString("username");
                int id = json.getInt("id");

                // Store user info to "Session" SharedPreferences
                SharedPreferences sharedPrefs = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);
                sharedPrefs.edit().putString("jwt", jwt).apply();
                sharedPrefs.edit().putString("username", json.getString("username")).apply();
                sharedPrefs.edit().putInt("id", json.getInt("id")).apply();

                startNextActivity();
            } else {
                json = new JSONObject(response.errorBody().string());
                Snackbar.make(getView(), json.getString("error"), Snackbar.LENGTH_LONG).show();
                Log.d(TAG, "onResponse: Failure, status " + response.code());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        LoadingScreenActivity activity = (LoadingScreenActivity) getActivity();
        Toast.makeText(activity, "Network error", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onFailure: Login call failed", t);
    }
}
