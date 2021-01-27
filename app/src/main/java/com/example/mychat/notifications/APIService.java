package com.example.mychat.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA7XEhvLM:APA91bENevU1MfVgXmAi65KpcHJ2ML-yNU5iO_DV1eBkJ3oWYvfptArhQgMmL6vTUWvkLXArWVYwquhWK_PjCb17rXeOZ0YZ1lAubDwsrBHO_8XYBTjSdhjQkLbUh6jqEW95zLJqGsPZ"

    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
