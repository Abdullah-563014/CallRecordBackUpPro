package com.uttam.callrecord.backuppro.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import com.uttam.callrecord.backuppro.BuildConfig;
import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.HomeActivity;
import com.uttam.callrecord.backuppro.R;
import com.uttam.callrecord.backuppro.Utils;
import com.uttam.callrecord.backuppro.database.Database;
import com.uttam.callrecord.backuppro.model.CallListModelClass;

import static com.uttam.callrecord.backuppro.CallRecorderApp.MY_NOTIFICATION_CHANNEL_ID;

public class MyService extends Service {

    private MediaRecorder recorder;
    private boolean recordStarted = false;
    private boolean serviceStarted = false;
    private boolean recordStart;
    private boolean serviceStart;
    private boolean hasException = false;
    private File file;
    private Database database;
    private String filePath;
    private String callIndicator;
    private CharSequence userDate;
    private CharSequence userMonth;
    private CharSequence userYear;
    private CharSequence userTime;
    private CharSequence fileTime;
    private MediaMetadataRetriever mmr;
    private Uri uri;
    private String duration;
    private String contactName = null;
    private int notificationId=1336;
    private String notificationTitle="Battery Warning";
    private String notificationContent="Please Turn off Notification For Power Saving";
    private String notificationTicker="Battery Warning";


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent != null) {
            recordStart = intent.getBooleanExtra("RecordStart", false);
            serviceStart = intent.getBooleanExtra("ServiceStart", false);
            if (serviceStart) {
                callIndicator = intent.getStringExtra("CallIndicator");
            }
        }

        if (serviceStart && Constants.phoneNumber != null && !serviceStarted) {
            startService();
        } else if (!serviceStart && serviceStarted) {
            stopService();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    public void startService() {
        if (serviceStart && !serviceStarted && callIndicator != null && Constants.phoneNumber != null) {
            if (!Constants.onForground) {
                Intent intent = new Intent(this, HomeActivity.class);
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
                            .setTicker(notificationTicker)
                            .setContentText(notificationContent)
                            .setSmallIcon(R.drawable.launcher_image_notification)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .build();
                } else {
                    notification = new NotificationCompat.Builder(
                            getBaseContext())
                            .setContentTitle(notificationTitle)
                            .setTicker(notificationTicker)
                            .setContentText(notificationContent)
                            .setSmallIcon(R.drawable.launcher_image_notification)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .build();
                }

                notification.flags = Notification.FLAG_NO_CLEAR;

                startForeground(notificationId, notification);

                startRecording();
            }

            Constants.onForground = true;
            serviceStarted = true;
            recordStarted = true;
        }
    }

    public void stopService() {
        if (!serviceStart && serviceStarted) {
            if (recordStarted) {
                stopRecording(Constants.phoneNumber);
                serviceStarted = false;
                recordStarted = false;
                callIndicator = null;
            }
            Constants.onForground = false;
            this.stopSelf();
        }
    }

    public Database getDatabaseInstance() {
        if (database == null) {
            database = new Database(getApplicationContext());
        }
        return database;
    }

    public MediaMetadataRetriever getMmrInstance() {
        if (mmr == null) {
            mmr = new MediaMetadataRetriever();
        }
        return mmr;
    }

    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        if (contactName == null) {
            contactName = phoneNumber;
        }
        return contactName;
    }

    public void startRecording() {
        if (recordStart && !recordStarted) {
            Log.d(Constants.TAG,"Call recording started");
            file = new File(Environment.getExternalStorageDirectory(), Constants.uploadFolderName);
            final Date date = new Date();
            userDate = DateFormat.format("dd", date.getTime());
            userMonth = DateFormat.format("MM", date.getTime());
            userYear = DateFormat.format("yyyy", date.getTime());
            userTime = DateFormat.format("hh.mm", date.getTime());
            fileTime = DateFormat.format("hh.mm.ss", date.getTime());


            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                if (!file.exists()) {
                    file.mkdirs();
                }
                filePath = file.getAbsolutePath() + "/" + userDate + "-" + userMonth + "-" + userYear + "-" + fileTime + "rec.3gp";
                recorder.setOutputFile(filePath);
                recorder.prepare();
                Thread.sleep(2000);
                recorder.start();
            } catch (Exception e) {
                hasException = true;
            }
            recordStarted = true;
        }
    }

    public void stopRecording(String phoneNumber) {
        if (recorder != null && recordStarted) {
            Log.d(Constants.TAG,"Call recording stopped");
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recordStarted = false;
                getDatabaseInstance();
                getMmrInstance();
                uri = Uri.parse(filePath);
                mmr.setDataSource(getApplicationContext(), uri);
                duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                contactName = getContactName(phoneNumber, getApplicationContext());
            } catch (Exception e) {
                hasException = true;
            }
            String blockPhoneNumber= Utils.getStringFromStorage(this,Utils.recordBlockListNumberKey,null);
            if (blockPhoneNumber==null){
                blockPhoneNumber="111";
            }
            if (blockPhoneNumber.contains(Constants.phoneNumber) || Constants.phoneNumber.contains(blockPhoneNumber)){
                //Block list number detected.
                hasException=true;
                deleteBlockCallRecordedFile();
            }
            if (database != null && !hasException) {
                database.initializedDatabase();
                String fileId="not set yet";
                database.insertData(String.valueOf(userDate), String.valueOf(userMonth), String.valueOf(userYear), new CallListModelClass(fileId,callIndicator, duration, contactName, String.valueOf(userTime), filePath));
                database.closeDatabase();
            }
            database = null;
            mmr = null;
            recorder = null;
        }
    }

    private void deleteBlockCallRecordedFile(){
        File file=new File(filePath);
        if (file.exists() && file.isFile()){
            try {
                file.delete();
                Log.d(Constants.TAG,"Block list call record deleted");
            }catch (Exception e){
                Log.d(Constants.TAG,"Block list call record deleted failed for "+e.getMessage());
            }
        }
    }

}
