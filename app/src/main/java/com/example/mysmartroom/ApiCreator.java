package com.example.mysmartroom;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCreator {
    static Api getApi(String ip){
        Log.d("ApiCreator", "Creating api for " + ip);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(Api.class);
    }
}
