package com.cse5236.bowlbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {
    private View v;
    private TextView usernameCircle;
    private TextView greeting;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_profile, container, false);

        SharedPreferences sp = getContext().getSharedPreferences("Session", Context.MODE_PRIVATE);
        usernameCircle = v.findViewById(R.id.profile_username);
        greeting = v.findViewById(R.id.profile_greeting);

        updateUsernameCircle(sp.getString("username", getString(R.string.username_placeholder)));
        greeting.setText(getString(R.string.profile_greeting, sp.getString("username", getString(R.string.username_placeholder))));

        return v;
    }

    private void updateUsernameCircle(String username) {
        Pattern usernamePattern = Pattern.compile("[A-Za-z0-9]{1,2}");
        Matcher usernameMatcher = usernamePattern.matcher(username);
        String displayed;

        if(usernameMatcher.find()) {
            displayed = usernameMatcher.group().toUpperCase();
        }else{
            displayed = username.substring(0, 2).toUpperCase();
        }

        usernameCircle.setText(displayed);
        return;
    }
}
