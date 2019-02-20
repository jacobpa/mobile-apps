package com.cse5236.bowlbuddy;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {
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
        userName = usernameField.getText().toString();
        password = passwordField.getText().toString();
        confirmPassword = confirmPasswordField.getText().toString();

        // Check that the password and confirm password are the same.
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Password and Confirmation Password did not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Make sure unique user name is used.

        Intent intent = new Intent(getActivity(), MasterListActivity.class);
        startActivity(intent);
        Toast.makeText(getActivity(), "Login not implemented.", Toast.LENGTH_SHORT)
                .show();
        // Finish current activity, so that the user cannot "back" into it.
        // TODO: Once user logs in, make MasterListActivity new launch activity
        getActivity().finish();
    }

    private void goBack() {
        getFragmentManager().popBackStack();
    }
}
