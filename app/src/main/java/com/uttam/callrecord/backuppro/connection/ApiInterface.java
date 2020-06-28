package com.uttam.callrecord.backuppro.connection;

import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.model.RequestNotificationModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @Headers({"Authorization: key="+Constants.serverKey, "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body RequestNotificationModel requestNotificationModel);
}
