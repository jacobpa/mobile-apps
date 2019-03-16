package com.cse5236.bowlbuddy.util;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class APISingleton {
    private static final String API_BASE = "https://bb.jacobpa.com/api/";
    private static APISingleton instance = null;

    private APIService service;

    private APISingleton() {
        service = new Retrofit.Builder()
            .baseUrl("https://bb.jacobpa.com/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(APIService.class);
    }

    public static APIService getInstance() {
        if (instance == null) {
            instance = new APISingleton();
        }

        return instance.service;
    }
}
