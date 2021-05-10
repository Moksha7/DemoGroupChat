package com.example.demogroupchat.notification;


import com.example.demogroupchat.notification.MyResponse;
import com.example.demogroupchat.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAACB1HPWw:APA91bG0VPuoDVxnXNRR-sjNevY0Ef_Ek_k1xvyFrNeDjRLfnH6hoRAVeAt2rYjFLL4CRteGg5inbzF91kF4jxl1W_TrBOo6VTB3vL3yPplHIB2o3CUaIbkniMzt2kZQ256MHtJ0TcGT"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
