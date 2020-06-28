package com.uttam.callrecord.backuppro.drive;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.HomeActivity;
import com.uttam.callrecord.backuppro.Utils;
import com.uttam.callrecord.backuppro.adapter.AllCallRecyclerViewAdapter;
import com.uttam.callrecord.backuppro.adapter.SubAdapter;
import com.uttam.callrecord.backuppro.adapter.ViewPagerAdapter;
import com.uttam.callrecord.backuppro.connection.ApiClient;
import com.uttam.callrecord.backuppro.connection.ApiInterface;
import com.uttam.callrecord.backuppro.fragment.AllCallFragment;
import com.uttam.callrecord.backuppro.fragment.IncomingCallFragment;
import com.uttam.callrecord.backuppro.fragment.OutgoingCallFragment;
import com.uttam.callrecord.backuppro.model.NotificationModel;
import com.uttam.callrecord.backuppro.model.RequestNotificationModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class DriveServiceHelper {

    private Context context;
    private final Executor executor= Executors.newSingleThreadExecutor();
    private Drive myDriveService;
    private Handler handler;
    private HomeActivity homeActivity;

    public DriveServiceHelper(Drive myDriveService,Context context){
        this.myDriveService=myDriveService;
        this.context=context;
        if (handler==null){
            handler=new Handler(Looper.getMainLooper());
        }
    }

    public void createFolder() {
        Tasks.call(executor,()->{
            try {
                File fileMetadata = new File();
                fileMetadata.setName("Offer50");
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                File file = myDriveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                Utils.setStringToStorage(context,Utils.folderIdKey,file.getId());
                return file.getId();
            }catch (Exception e){
                return null;
            }
        });
    }

    public Task<String> createFile(String path, String fileName) {
        return Tasks.call(executor,()->{
            File myFile=null;
            try{
                File fileMetadata=new File();
                if (Utils.getStringFromStorage(context,Utils.folderIdKey,null)!=null){
                    fileMetadata.setParents(Collections.singletonList(Utils.getStringFromStorage(context,Utils.folderIdKey,null)));
                }
                fileMetadata.setName(fileName);

                java.io.File file=new java.io.File(path);

                FileContent mediaContent=new FileContent("video/3gpp",file);
                myFile=myDriveService.files().create(fileMetadata,mediaContent).setFields("id").execute();
                sendNotificationToAdmin();
            }catch (Exception e){
                e.printStackTrace();
                Log.d(Constants.TAG,"creating file to drive failed for "+e.getMessage());
            }
            if (myFile==null){
                throw new IOException("Null result when file creation");
            }
            return myFile.getId();
        });
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(executor, new Callable<FileList>() {
            @Override
            public FileList call() throws Exception {
                return myDriveService
                        .files()
                        .list()
                        .setFields("nextPageToken, files(id, name)")
                        .execute();
            }
        });
    }

    public void deleteFile(HomeActivity homeActivity, String fileId) {
        Tasks.call(executor, ()->{
            try {
                myDriveService.files().delete(fileId).execute();
                Log.d(Constants.TAG,"filed deleted successfully");
                handler.post(new ToastRunnable(context,"File deleted successfully"));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        homeActivity.recreate();
                    }
                });
            }catch (Exception e){
                Log.d(Constants.TAG,"failed to deleted file for "+e.getMessage());
                handler.post(new ToastRunnable(context,"failed to deleted file for "+e.getMessage()));
            }
            return null;
        });
    }

    public void downloadDriveFile(Context context, String fileId, String fileNameAndPath) {

        Tasks.call(executor,()->{
            try{
                OutputStream output = new FileOutputStream(fileNameAndPath);
                myDriveService.files().get(fileId)
                        .executeMediaAndDownloadTo(output);
                output.flush();
                output.close();
                Log.d(Constants.TAG,"download successful");
                handler.post(new ToastRunnable(context,"Download successful"));
                if (homeActivity==null){
                    homeActivity= (HomeActivity) context;
                }
                if (handler!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            homeActivity.recreate();
                        }
                    });
                }
            }catch (Exception e){
                Log.d(Constants.TAG,"failed to download for "+e.getMessage());
                handler.post(new ToastRunnable(context,"Download failed for "+e.getMessage()));
            }
            return null;
        });
    }

    class ToastRunnable implements Runnable {
        private Context context;
        private String message;


        public ToastRunnable(Context context, String message) {
            this.context = context;
            this.message=message;
        }

        @Override
        public void run() {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

//    public Task<FileList> queryFiles() {
//        return Tasks.call(executor, new Callable<FileList>() {
//            @Override
//            public FileList call() throws Exception {
//                Drive.Files.List request = myDriveService.files().list();
//                return request.setQ("'"+Utils.getStringFromStorage(context,Utils.folderIdKey,null)+"' in parents and trashed=false").execute();
//            }
//        });
//    }


    private void sendNotificationToAdmin() {
        String userEmail=Utils.getStringFromStorage(context,Utils.userEmailKey,null);
        String topicsName=null;
        if (userEmail!=null){
            String[] userNameFromEmail=userEmail.split("@");
            topicsName=userNameFromEmail[0];
            RequestNotificationModel rootModel = new RequestNotificationModel("/topics/"+topicsName, new NotificationModel("Body", "Title"));

            ApiInterface apiService =  ApiClient.getApiClient().create(ApiInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendNotification(rootModel);

            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    Log.d(Constants.TAG,"Notification sending status is "+response.isSuccessful());
                    if (response.isSuccessful()){
                        Log.d(Constants.TAG,"Notification sending message is "+response.message());
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                    Log.d(Constants.TAG,"Failed to send notification for "+t.getMessage());
                }
            });
        }
    }

}
