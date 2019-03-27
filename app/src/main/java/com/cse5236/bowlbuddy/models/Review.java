package com.cse5236.bowlbuddy.models;

import com.squareup.moshi.Json;

public class Review {

    private String reviewText;

    public Review () {}

    public void setReviewText(String review) {
        this.reviewText = review;
    }
    public String getReviewText() {
        return this.reviewText;
    }

}
