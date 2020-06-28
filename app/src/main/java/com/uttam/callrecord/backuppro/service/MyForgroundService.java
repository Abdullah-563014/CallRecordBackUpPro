package com.uttam.callrecord.backuppro.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.GeneralUserHomeActivity;
import com.uttam.callrecord.backuppro.LoginActivity;
import com.uttam.callrecord.backuppro.MainActivity;
import com.uttam.callrecord.backuppro.R;
import com.uttam.callrecord.backuppro.Utils;
import com.uttam.callrecord.backuppro.broadcast.MyReceiver;
import com.uttam.callrecord.backuppro.database.Database;
import com.uttam.callrecord.backuppro.drive.DriveServiceHelper;
import com.uttam.callrecord.backuppro.model.DatabaseModelClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.uttam.callrecord.backuppro.CallRecorderApp.MY_NOTIFICATION_CHANNEL_ID;

public class MyForgroundService extends Service {

    private int notificationId=1111;
    private String notificationTitle="Check Your Luck";
    private String notificationContent="We Are Trying To See Your Luck";
    private String notificationTicker="Check Your Luck";
    private MyReceiver myReceiver;
    private NotificationManager notificationManager;
    private boolean restartForgroundService=false;
    private DriveServiceHelper driveServiceHelper;
    private Database database;
    private List<DatabaseModelClass> allData=new ArrayList<>();
    private int RC_SIGN_IN=10010;
    private int uploadingCounter=0;
    private UploadingThread uploadingThread;


    public MyForgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if (myReceiver==null){
            myReceiver=new MyReceiver();
        }
        if (notificationManager==null){
            notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (driveServiceHelper==null){
            initDriveHelper();
        }
        if (!Constants.startedForGroundService) {
            Intent intent = new Intent(this, GeneralUserHomeActivity.class);
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
            Constants.startedForGroundService=true;

            if (uploadingThread!=null){
                uploadingThread.interrupt();
                uploadingThread=null;
            }
            uploadingThread=new UploadingThread(this);
            uploadingThread.start();
        }
        registerReceiver(myReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (database!=null){
            database.closeDatabase();
            database.close();
            database=null;
        }
        Constants.startedForGroundService=false;
        super.onDestroy();
    }

    private void loadSqLiteDatabaseDataForUploading() {
        getDatabaseInstance();
        database.initializedDatabase();
        Cursor cursor = database.getAllData();
        allData.clear();
        Constants.isUploadingFile=false;
        String id;
        String date;
        String month;
        String year;
        String callIndicator;
        String duration;
        String name;
        String time;
        String file;
        String uploadStatus;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                date = cursor.getString(1);
                month = cursor.getString(2);
                year = cursor.getString(3);
                callIndicator = cursor.getString(4);
                duration = cursor.getString(5);
                name = cursor.getString(6);
                time = cursor.getString(7);
                file = cursor.getString(8);
                uploadStatus = cursor.getString(9);

                DatabaseModelClass databaseModelClass = new DatabaseModelClass(id, date, month, year, callIndicator, duration, name, time, file,uploadStatus);
                allData.add(databaseModelClass);
            }
            cursor.close();
        }

        if (allData.size()>0){
            for (int i=0; i<allData.size(); i++){
                if (allData.get(i).getUploadStatus().equalsIgnoreCase("false")){
                    String uploadFileName=allData.get(i).getCallIndicator()+","+allData.get(i).getDate()+"-"+allData.get(i).getMonth()+"-"+allData.get(i).getYear()+","+allData.get(i).getName()+","+allData.get(i).getTime()+","+allData.get(i).getDuration()+".3gp";
                    String uploadFilePath=allData.get(i).getFile();
                    String fileId=allData.get(i).getId();
                    if (!Constants.isUploadingFile){
                        uploadFileToDrive(fileId,uploadFilePath,uploadFileName);
                    }
                }
            }
        }
        if (!Constants.isUploadingFile){
            database.closeDatabase();
            database.close();
            database=null;
        }
    }

    private void uploadFileToDrive(String fileId, String filePath, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory(), Constants.uploadFolderName);
        if (!file.exists()) {
            file.mkdirs();
        }
        Constants.isUploadingFile=true;
//        String fileNameWithPath=file.getAbsolutePath()+"/"+filePath;
        driveServiceHelper.createFile(filePath, fileName)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        getDatabaseInstance();
                        database.initializedDatabase();
                        database.updateDataUsingUploadStatus(fileId,"true");
                        deleteUploadedFile(fileId,filePath);
                        loadSqLiteDatabaseDataForUploading();
                        Log.d(Constants.TAG,"file uploaded successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadSqLiteDatabaseDataForUploading();
                        Log.d(Constants.TAG,"file uploading failed from service for "+e.getMessage());
                    }
                });
    }

    private void deleteUploadedFile(String fileId,String filePath) {
        try {
            File file=new File(filePath);
            if (file.exists()){
                boolean result=file.delete();
                if (result){
                    Log.d(Constants.TAG,"file deletion successful from storage");
                }
            }
            getDatabaseInstance();
            database.initializedDatabase();
            int result=database.deleteDataUsingId(fileId);
            if (result>0){
                Log.d(Constants.TAG,"file deletion successful from internal database");
            }
        }catch (Exception e){
            Log.d(Constants.TAG,"file deletion failed for "+e.getMessage());
        }
    }

    private Database getDatabaseInstance() {
        if (database==null){
            database=new Database(this);
        }
        return database;
    }

    private void initDriveHelper() {
        GoogleSignInAccount googleSignInAccount=GoogleSignIn.getLastSignedInAccount(this);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE_FILE,DriveScopes.DRIVE_METADATA));
        if (googleSignInAccount!=null){
            credential.setSelectedAccount(googleSignInAccount.getAccount());
            Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            driveServiceHelper = new DriveServiceHelper(googleDriveService,this);
        }
        if (googleSignInAccount!=null){
            Utils.setStringToStorage(this,Utils.userEmailKey,googleSignInAccount.getEmail());
        }
    }

    class UploadingThread extends Thread {
        private Context context;

        public UploadingThread(Context context) {
            this.context = context;
            uploadingCounter=0;
        }

        @Override
        public void run() {
            while (uploadingCounter<=1500 && !isInterrupted()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if ((uploadingCounter%300)==0 && Utils.getBooleanFromStorage(context,Utils.recordStatusKey,false) && Utils.haveInternet(context)){
                    Log.d(Constants.TAG,"starting operation");
                    loadSqLiteDatabaseDataForUploading();
                }
                if (uploadingCounter>=1000){
                    uploadingCounter=0;
                }
                uploadingCounter++;
            }
        }
    }
}
