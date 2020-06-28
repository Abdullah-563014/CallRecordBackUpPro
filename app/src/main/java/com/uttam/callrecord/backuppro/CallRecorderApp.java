package com.uttam.callrecord.backuppro;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class CallRecorderApp extends Application {
    public static final String MY_NOTIFICATION_CHANNEL_ID="VoiceCall";
    private String notificationName="Voice Call";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT>=26){
            NotificationChannel notificationChannel=new NotificationChannel(
                    MY_NOTIFICATION_CHANNEL_ID
                    ,notificationName
                    , NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }
}
