package com.cse5236.bowlbuddy.util;

import com.cse5236.bowlbuddy.models.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {
    // User and Session endpoints
    /**
     * Register a new user in the database.
     * @param username The desired username (only [A-Za-z0-9_]
     * @param password The user's password
     * @param password_confirmation Confirmation of the users password
     * @return A call containing a User object
     */
    @POST("users")
    Call<User> signUp(@Query("username") String username,
                @Query("password") String password,
                @Query("password_confirmation") String password_confirmation);

    // Type of okhttp3.ResponseBody, because we do not want to deserialize response to Java Object

    /**
     * Log a user in to receive a JWT token.
     * @param username The user's username
     * @param password The user's password
     * @return A ResponseBody containing the JWT token or an error.
     */
    @POST("login")
    Call<ResponseBody> login(@Query("username") String username,
                             @Query("password") String password);

    /**
     * Update a user's username.
     * @param id The id of the user
     * @param username The new desired username
     * @return A User object with the new information
     */
    @PATCH("users/{id}")
    Call<User> updateUsername(@Path("id") int id,
                              @Query("username") String username,
                              @Header("Authorization") String token);
}
