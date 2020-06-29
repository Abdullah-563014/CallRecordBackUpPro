package com.uttam.callrecord.backuppro;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.uttam.callrecord.backuppro.adapter.ViewPagerAdapter;
import com.uttam.callrecord.backuppro.drive.DriveServiceHelper;
import com.uttam.callrecord.backuppro.fragment.AllCallFragment;
import com.uttam.callrecord.backuppro.fragment.IncomingCallFragment;
import com.uttam.callrecord.backuppro.fragment.OutgoingCallFragment;
import com.uttam.callrecord.backuppro.model.CallListModelClass;
import com.uttam.callrecord.backuppro.model.DatabaseModelClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    public DriveServiceHelper driveServiceHelper;
    public List<DatabaseModelClass> databaseModelClassArrayList=new ArrayList<>();
    private String userPaidStatus,todayTotalRecordDownload,recordLastDownloadTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initialAll();

        if (driveServiceHelper==null){
            initDriveHelper();
        }
    }


    public void initialAll() {
        tabLayout = findViewById(R.id.homeActivityTabLayoutId);
        viewPager = findViewById(R.id.homeActivityViewPagerId);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new AllCallFragment(), "All Call");
        viewPagerAdapter.addFragment(new IncomingCallFragment(), "Incoming Call");
        viewPagerAdapter.addFragment(new OutgoingCallFragment(), "Outgoing Call");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }
    }

    private void initDriveHelper() {
        GoogleSignInAccount googleSignInAccount= GoogleSignIn.getLastSignedInAccount(this);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE_FILE,DriveScopes.DRIVE_METADATA));
        if (googleSignInAccount!=null){
            credential.setSelectedAccount(googleSignInAccount.getAccount());
            Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            driveServiceHelper = new DriveServiceHelper(googleDriveService,this);
        }
    }

    public void recordPlayButtonClicked(String fileId, String fileName) {
        File folderName=new File(Environment.getExternalStorageDirectory(),Constants.downloadFolderName);
        if (!folderName.exists()) {
            folderName.mkdirs();
        }
        String filePath = folderName.getAbsolutePath() +"/"+fileName;
        File file=new File(filePath);
        if (file.exists() && file.isFile()){
//            Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
//            intent.putExtra("uri", filePath);
//            startActivity(intent);
            try {
                PackageManager pm = getPackageManager();
                PackageInfo info=pm.getPackageInfo("com.google.android.music", PackageManager.GET_ACTIVITIES);
                Log.d(Constants.TAG,"package info is "+info.activities.length);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.music");

                if (Build.VERSION.SDK_INT>=24){
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri fileUri= FileProvider.getUriForFile(HomeActivity.this,BuildConfig.APPLICATION_ID+".provider",file);
                    intent.setDataAndType(fileUri, "audio/*");
                }else {
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                }
                startActivity(intent);
            }
            catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(HomeActivity.this, "Music player app not installed in your phone", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.music"));
                startActivity(intent);
                Log.d(Constants.TAG,"failed to start music player for "+e.getMessage());
            }
        }else {
            initDownloadOperation(fileId,filePath);
        }

    }

    private void alertDialogForDownloadingFileFromDrive(String id,String name) {
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this)
                .setCancelable(false)
                .setMessage("Do you want to download this file from server?")
                .setTitle("File not exist yet.")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        driveServiceHelper.downloadDriveFile(HomeActivity.this,id,name);
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog=builder.create();
        if (!isFinishing()){
            alertDialog.show();
        }
    }

    public void recordShareButtonClicked(String fileId, String fileName) {
        File folderName=new File(Environment.getExternalStorageDirectory(),Constants.downloadFolderName);
        if (!folderName.exists()) {
            folderName.mkdirs();
        }
        String filePath = folderName.getAbsolutePath() +"/"+fileName;
        File file=new File(filePath);
        if (file.exists() && file.isFile()){
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("video/3gpp");

            if (Build.VERSION.SDK_INT>=24){
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri fileUri= FileProvider.getUriForFile(HomeActivity.this,BuildConfig.APPLICATION_ID+".provider",file);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
            }else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
            }
            startActivity(Intent.createChooser(share, "Share Recorded File"));
        }else {
            initDownloadOperation(fileId,filePath);
        }

    }

    public void alertDialogForDeletingFileFromDrive(String id, String fileName) {
        File folderName=new File(Environment.getExternalStorageDirectory(),Constants.downloadFolderName);
        if (!folderName.exists()) {
            folderName.mkdirs();
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this)
                .setCancelable(false)
                .setMessage("Do you want to delete this file?")
                .setTitle("Confirmation")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        driveServiceHelper.deleteFile(HomeActivity.this,id);
                        String filePath = folderName.getAbsolutePath() +"/"+fileName;
                        File file=new File(filePath);
                        if (file.exists() && file.isFile()){
                            file.delete();
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog=builder.create();
        if (!isFinishing()){
            alertDialog.show();
        }
    }

    private void initDownloadOperation(String fileId, String filePath) {
        userPaidStatus=Utils.getStringFromStorage(HomeActivity.this, Utils.userPaidStatusKey,null);
        todayTotalRecordDownload=Utils.getStringFromStorage(HomeActivity.this, Utils.todayTotalRecordDownloadKey,null);
        recordLastDownloadTime=Utils.getStringFromStorage(HomeActivity.this, Utils.recordLastDownloadTimeKey,null);
        if (todayTotalRecordDownload==null){
            todayTotalRecordDownload="0";
        }

        if (userPaidStatus!=null){
            if (userPaidStatus.equalsIgnoreCase("false")){
                if (recordLastDownloadTime!=null && recordLastDownloadTime.equalsIgnoreCase(Utils.getCurrentTime())){
                    int downloadCount=Integer.parseInt(todayTotalRecordDownload);
                    if (downloadCount<=1){
                        alertDialogForDownloadingFileFromDrive(fileId,filePath);
                    }else {
                        recordDownloadLimitExcessAlertDialog();
                    }
                }else {
                    Utils.setStringToStorage(HomeActivity.this,Utils.todayTotalRecordDownloadKey,"0");
                    alertDialogForDownloadingFileFromDrive(fileId,filePath);
                }
            }else {
                alertDialogForDownloadingFileFromDrive(fileId,filePath);
            }
        }else {
            Toast.makeText(this, "Sorry your paid status is unable to detect, Please try again later.", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }

    private void recordDownloadLimitExcessAlertDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this)
                .setCancelable(false)
                .setMessage(getResources().getString(R.string.record_download_limit_excess_message))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
    }


}
