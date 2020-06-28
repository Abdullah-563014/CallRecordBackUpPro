package com.uttam.callrecord.backuppro.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.MainActivity;
import com.uttam.callrecord.backuppro.R;
import com.uttam.callrecord.backuppro.Utils;

import static com.uttam.callrecord.backuppro.CallRecorderApp.MY_NOTIFICATION_CHANNEL_ID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static int notificationId=123123;

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String userEmail=Utils.getStringFromStorage(this,Utils.userEmailKey,null);
        if (userEmail!=null){
            String[] fragile=userEmail.split("@");
            String topics=fragile[0];
            if (Utils.getBooleanFromStorage(this,Utils.recordStatusKey,true)){
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topics);
            }else {
                FirebaseMessaging.getInstance().subscribeToTopic(topics);
            }
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification()!=null){
            showNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }
    }

    private void showNotification(String notificationTitle, String notificationBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getBaseContext(), 0, intent, 0);
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = new NotificationCompat.Builder(
                    getBaseContext(), MY_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(notificationTitle)
                    .setTicker(notificationTitle)
                    .setContentText(notificationBody)
                    .setSmallIcon(R.drawable.launcher_image_notification)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(
                    getBaseContext())
                    .setContentTitle(notificationTitle)
                    .setTicker(notificationTitle)
                    .setContentText(notificationBody)
                    .setSmallIcon(R.drawable.launcher_image_notification)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();
        }
        notification.flags = Notification.FLAG_NO_CLEAR;
        NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,notification);
    }
}
