package com.cse5236.bowlbuddy.util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A retorfit Callback class with common defaults, for use with the BowlBuddy API.
 *
 * @param <T> Successful response body type.
 */
public abstract class BowlBuddyCallback<T> implements Callback<T> {
    private final static String TAG = BowlBuddyCallback.class.getCanonicalName();
    private Context context;  // Need application context to make Toasts in onFailure
    private View view;

    public BowlBuddyCallback() {
        // Empty constructor
    }

    public BowlBuddyCallback(Context context, View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Toast.makeText(context, "Network error occurred.", Toast.LENGTH_LONG).show();
        Log.e(TAG, "onFailure: Network error:", t);
    }

    /**
     * Parse a response's error, and draw a Snackbar with the received error message.
     *
     * @param response The response whose error is bing parsed
     */
    public void parseError(Response<T> response) {
        try {
            JSONObject json = new JSONObject(response.errorBody().string());
            Snackbar.make(view, json.getString("error"), Snackbar.LENGTH_LONG)
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
