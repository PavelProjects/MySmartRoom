package com.example.mysmartroom;

import com.example.mysmartroom.objects.DeviceAction;
import com.example.mysmartroom.objects.DeviceInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {
    @GET("/json")
    Call<DeviceInfo> getDeviceInfo();

    @POST("/smarthome")
    Call<DeviceInfo> sendActions(@Body List<DeviceAction> actions);

    @GET("/restart")
    Call<Void> restart();
}
