package com.cse5236.bowlbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cse5236.bowlbuddy.models.Review;
import com.cse5236.bowlbuddy.models.User;
import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BowlBuddyCallback;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private final static String TAG = ProfileFragment.class.getCanonicalName();

    private View view;
    private TextView usernameCircle;
    private TextView reviewCounter;
    private EditText usernameField;
    private final EditText[] passwordFields = new EditText[2];
    private APIService service;
    private SharedPreferences sharedPreferences;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get instance of service to communicate with the server
        service = APISingleton.getInstance();

        // Get shared user information
        sharedPreferences = getContext().getSharedPreferences("Session", Context.MODE_PRIVATE);
        usernameCircle = view.findViewById(R.id.profile_username);
        TextView greeting = view.findViewById(R.id.profile_greeting);
        reviewCounter = view.findViewById(R.id.review_counter);
        service.getUserReviews(sharedPreferences.getInt("id", 0), sharedPreferences.getString("jwt", "")).enqueue(new GetReviewsCallback(getContext(), view));

        updateUsernameCircle(sharedPreferences.getString("username", getString(R.string.username_placeholder)));
        greeting.setText(getString(R.string.profile_greeting, sharedPreferences.getString("username", getString(R.string.username_placeholder))));

        Button usernameChangeButton = view.findViewById(R.id.username_change_submit);
        Button passwordChangeButton = view.findViewById(R.id.password_change_submit);
        Button deleteAccountButton = view.findViewById(R.id.delete_account_btn);

        usernameChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUsername();
            }
        });
        passwordChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
            }
        });

        usernameField = view.findViewById(R.id.new_username_field);
        passwordFields[0] = view.findViewById(R.id.password_change_field);
        passwordFields[1] = view.findViewById(R.id.password_change_confirm_field);

        return view;
    }

    /**
     * Method used to update the username
     *
     * @param username The new user name
     */
    private void updateUsernameCircle(String username) {
        // Check username formatting
        Pattern usernamePattern = Pattern.compile("[A-Za-z0-9]{1,2}");
        Matcher usernameMatcher = usernamePattern.matcher(username);
        String displayed;

        if (usernameMatcher.find()) {
            displayed = usernameMatcher.group().toUpperCase();
        } else {
            displayed = username.substring(0, 2).toUpperCase();
        }

        // Change the text of the username
        usernameCircle.setText(displayed);
    }

    /**
     * Method used to change the username
     */
    private void changeUsername() {
        // Get the new username
        String desiredUsername = usernameField.getText().toString();

        // If username is valid, update the username to the server
        if (User.isUsernameValid(desiredUsername)) {
            service.updateUsername(sharedPreferences.getInt("id", -1),
                    desiredUsername,
                    sharedPreferences.getString("jwt", "")).enqueue(new ChangeUsernameCallback(getContext(), view));
        } else {
            Snackbar.make(view, "Username must only contain letters, digits, or underscores.", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Method used to change the password
     */
    private void changePassword() {
        // Get the new password and confirmation password
        String newPassword = passwordFields[0].getText().toString();
        String newPasswordConfirmation = passwordFields[1].getText().toString();

        // Make sure both passwords match
        boolean passwordsMatch = newPassword.equals(newPasswordConfirmation);
        boolean fieldsEmpty = newPassword.isEmpty() || newPasswordConfirmation.isEmpty();

        if (!passwordsMatch) {
            Snackbar.make(view, "Password and Confirmation Password did not match.", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        if (fieldsEmpty) {
            Snackbar.make(view, "Password fields can not be empty.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Update the new password to the server
        service.updatePassword(sharedPreferences.getInt("id", -1), newPassword, sharedPreferences.getString("jwt", ""))
                .enqueue(new ChangePasswordCallback(getContext(), view));
    }

    /**
     * Method used to delete a user
     */
    private void deleteUser() {
        // Send delete request to the server
        service.deleteUser(sharedPreferences.getInt("id", -1), sharedPreferences.getString("jwt", "")).enqueue(new DeleteAccountCallback(getContext(), view));
    }

    /**
     * Method used to refresh the information on this fragment
     */
    private void refreshFragment() {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.profiler_container);
        getFragmentManager().beginTransaction()
                .detach(currentFragment)
                .attach(currentFragment)
                .commit();
    }

    private class ChangeUsernameCallback extends BowlBuddyCallback<User> {
        ChangeUsernameCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
            if (response.isSuccessful()) {
                User u = response.body();
                Log.d(TAG, "onResponse: Username is " + u.getUsername());
                sharedPreferences.edit().putString("username", u.getUsername()).apply();

                refreshFragment();
            } else {
                parseError(response);
            }
        }
    }

    private class ChangePasswordCallback extends BowlBuddyCallback<User> {
        ChangePasswordCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
            if (response.isSuccessful()) {
                // Don't need to serialize User object
                Snackbar.make(view, "Password changed successfully.", Snackbar.LENGTH_LONG).show();
            } else {
                parseError(response);
            }
        }
    }

    private class DeleteAccountCallback extends BowlBuddyCallback<Void> {
        DeleteAccountCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
            if (response.isSuccessful()) {
                sharedPreferences.edit().clear().apply();
                Intent i = new Intent(getActivity(), LoadingScreenActivity.class);
                startActivity(i);

                getActivity().finish();
            } else {
                parseError(response);
            }
        }
    }

    private class GetReviewsCallback extends BowlBuddyCallback<List<Review>> {
        GetReviewsCallback(Context context, View view) {
            super(context, view);
        }

        @Override
        public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
            if (response.isSuccessful()) {
                reviewCounter.setText(String.format(Locale.getDefault(), "%d", (response.body().size())));
            } else {
                reviewCounter.setText(R.string.unknown);
                parseError(response);
            }
        }
    }
}
