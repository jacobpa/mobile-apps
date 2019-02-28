package com.cse5236.bowlbuddy.util;

import com.cse5236.bowlbuddy.models.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIService {
    @POST("users")
    Call<User> signUp(@Query("username") String username,
                @Query("password") String password,
                @Query("password_confirmation") String password_confirmation);

    // Type of okhttp3.ResponseBody, because we do not want to deserialize response to Java Object
    @POST("login")
    Call<ResponseBody> login(@Query("username") String username,
                             @Query("password") String password);
}
