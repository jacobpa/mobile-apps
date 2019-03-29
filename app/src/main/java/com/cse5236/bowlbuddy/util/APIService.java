package com.cse5236.bowlbuddy.util;

import com.cse5236.bowlbuddy.models.Review;
import com.cse5236.bowlbuddy.models.User;
import com.cse5236.bowlbuddy.models.Bathroom;
import com.cse5236.bowlbuddy.models.Building;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {
    // User and Session endpoints

    /**
     * Register a new user in the database.
     *
     * @param username              The desired username (only [A-Za-z0-9_]
     * @param password              The user's password
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
     *
     * @param username The user's username
     * @param password The user's password
     * @return A ResponseBody containing the JWT token or an error.
     */
    @POST("login")
    Call<ResponseBody> login(@Query("username") String username,
                             @Query("password") String password);

    /**
     * Update a user's username.
     *
     * @param id       The id of the user
     * @param username The new desired username
     * @param token    The user's JWT authentication token
     * @return A Call object of type User with the new information
     */
    @PATCH("users/{id}")
    Call<User> updateUsername(@Path("id") int id,
                              @Query("username") String username,
                              @Header("Authorization") String token);

    /**
     * Update a user's password.
     *
     * @param id       The id of the user
     * @param password The new desired password
     * @param token    The user's JWT authentication token
     * @return A Call object of type User with the new information
     */
    @PATCH("users/{id}")
    Call<User> updatePassword(@Path("id") int id,
                              @Query("password") String password,
                              @Header("Authorization") String token);

    /**
     * Delete a user from the database.
     *
     * @param id    The id of the user
     * @param token The user's JWT authentication token
     * @return void object
     */
    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int id,
                          @Header("Authorization") String token);

    /**
     * Get list of all bathrooms from the database.
     *
     * @param token The user's JWT authentication token
     * @return A Call object of type List<Bathroom> that contains all bathrooms
     */
    @GET("bathrooms")
    Call<List<Bathroom>> getAllBathrooms(@Header("Authorization") String token);

    /**
     * Get all buildings from the database.
     *
     * @param token The user's JWT authentication token
     * @return A Call object of type List<Building> that contains all buildings
     */
    @GET("buildings")
    Call<List<Building>> getAllBuildings(@Header("Authorization") String token);

    /**
     * Get relevant keys of a specific building from the database.
     *
     * @param id The id of the bathroom that is being queried
     * @param token The user's JWT authentication token
     * @return A Call object of type Building with information about the queried building
     */
    @GET("buildings/{id}")
    Call<Building> getLocation(@Path("id") int id,
                               @Header("Authorization") String token);

    /**
     * Post a review about a bathroom from the root endpoint to the server.
     *
     * @param id The id of the user posting the review
     * @param bathroomId The id of the bathroom being reviewed
     * @param details The text review about the bathroom
     * @param token The user's JWT authentication token
     * @return void object
     */
    @POST("reviews")
    Call<Void> addReview(@Query("user_id") int id,
                         @Query("bathroom_id") int bathroomId,
                         @Query("details") String details,
                         @Header("Authorization") String token);

    /**
     * Get reviews made by a user from the server.
     *
     * @param id The id of the user who's reviews will be queried
     * @param token The user's JWT authentication token
     * @return A Call object of type List<Review> that contains all reviews made by this user
     */
    @GET("users/{user_id}/reviews")
    Call<List<Review>> getUserReviews(@Path("user_id") int id,
                                      @Header("Authorization") String token);

    /**
     * Get information about a given user from the server.
     *
     * @param userID The id of the user who's information will be queried
     * @param token The user's JWT authentication token
     * @return A Call object of type User that contains information about the user
     */
    @GET("users/{id}")
    Call<User> getUser(@Path("id") int userID,
                       @Header("Authorization") String token);

    /**
     * Get all the reviews made about a specific bathroom from the server.
     *
     * @param bathroomID The id of the bathroom who's reviews will be queried
     * @param token The user's JWT authentication token
     * @return A Call object of type List<Review> that contains all of the reviews for a bathroom
     */
    @GET("bathrooms/{id}/reviews")
    Call<List<Review>> getBathroomReviews(@Path("id") int bathroomID,
                                          @Header("Authorization") String token);

    /**
     * Get information about a specific bathroom from the server.
     *
     * @param id The id of the bathroom who's information will be queried
     * @param token The user's JWT authentication token
     * @return A Call object of type Bathroom that contains all of the information for a bathroom
     */
    @GET("bathrooms/{id}")
    Call<Bathroom> getBathroom(@Path("id") int id,
                               @Header("Authorization") String token);

    /**
     * Delete a user review from the server.
     *
     * @param userID The id of the user's review that will be deleted
     * @param id The id of the review that will be deleted
     * @param token The user's JWT authentication token
     * @return A void object
     */
    @DELETE("users/{user_id}/reviews/{id}")
    Call<Void> deleteReview(@Path("user_id") int userID,
                            @Path("id") int id,
                            @Header("Authorization") String token);

    /**
     * Push an update to a user's review to the server.
     *
     * @param userID The id of the user who's review will be updated
     * @param id The id of the review that will be updated
     * @param reviewStr The updated text of the review
     * @param token The user's JWT authentication token
     * @return A void object
     */
    @PATCH("users/{user_id}/reviews/{id}")
    Call<Void> updateUserReview(@Path("user_id") int userID,
                                @Path("id") int id,
                                @Query("details") String reviewStr,
                                @Header("Authorization") String token);

    /**
     * Get a list of a user's favorite bathrooms from the server
     *
     * @param userID The id of the user who's favorites list will be queried
     * @param token The user's JWT authentication token
     * @return A Call object of type List<Bathroom> that contains all the favorite bathrooms of a user
     */
    @GET("users/{id}/favorites")
    Call<List<Bathroom>> getFavorites(@Path("id") int userID,
                                      @Header("Authorization") String token);

    /**
     * Post a new favorite bathroom for a user to the server.
     *
     * @param userID The id of the user who is adding a favorite bathroom
     * @param bathroomID The id of the bathroom that is being added to favorites
     * @param token The user's JWT authentication token
     * @return A Call object that contains a list of a user's favorite bathrooms
     */
    @POST("users/{id}/favorites")
    Call<List<Bathroom>> addFavorite(@Path("id") int userID,
                                     @Query("bathroom_id") int bathroomID,
                                     @Header("Authorization") String token);

    /**
     * Delete a bathroom from a list of favorites in the server.
     *
     * @param userID The id of the user removing a bathroom from their favorites
     * @param bathroomID The id of the bathroom being removed
     * @param token The user's JWT authentication token
     * @return A Call object that contains a list of all the user's favorite bathrooms
     */
    @DELETE("users/{id}/favorites")
    Call<List<Bathroom>> deleteFavorite(@Path("id") int userID,
                                        @Query("bathroom_id") int bathroomID,
                                        @Header("Authorization") String token);
}
