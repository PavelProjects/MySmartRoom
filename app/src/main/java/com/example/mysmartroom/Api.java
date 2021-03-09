package com.example.mysmartroom;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Api {
    @GET("/json")
    Call<DeviceInfo> getDeviceInfo();

    @GET("/json")
    Call<DeviceInfo> saveSettings(@Query("open_set") String openValue, @Query("close_set") String closeValue, @Query("angle_set") String angleValue, @Query("auto_set") String auto, @Query("device_name_set") String deviceName);

    @GET("/json/open")
    Call<DeviceInfo> open(@Query("servo") String servo);

    @GET("/json/close")
    Call<DeviceInfo> close(@Query("servo") String servo);

    @GET("/json/middle")
    Call<DeviceInfo> middle(@Query("servo") String servo);
}
