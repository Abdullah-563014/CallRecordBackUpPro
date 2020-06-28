package com.uttam.callrecord.backuppro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uttam.callrecord.backuppro.model.AdminNoticeModelClass;
import com.uttam.callrecord.backuppro.service.MyFirebaseMessagingService;
import com.uttam.callrecord.backuppro.service.MyForgroundService;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView timeRemainingTextView, couponCodeTextView;
    private Button copyCouponCodeButton, resetTimerButton,hideAppButton;
    private ImageView adminNoticeImageView;
    private ClipboardManager clipboardManager;
    private String couponCode;
    private int remainingTime;
    private Handler tapHandler;
    private Runnable tapRunnable;
    private int mTapCount = 0;
    private int milSecDelay = 1000;
    private CountDownTimerThread countDownTimerThread;
    private NotificationManager notificationManager;
    private DatabaseReference databaseReference;
    private AdminNoticeModelClass adminNoticeModelClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initAll();

        getRemainingTime();

        startCountDownTimer();

        registerBroadCastReceiverService(false);

        getAdminMessageFromDatabase();

    }


    private void shareMyApplication() {
        try {
//            StrictMode.VmPolicy.Builder builder=new StrictMode.VmPolicy.Builder();
//            StrictMode.setVmPolicy(builder.build());
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
            File srcFile = new File(ai.publicSourceDir);
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType("*/*");
            if (Build.VERSION.SDK_INT>=24){
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri fileUri= FileProvider.getUriForFile(MainActivity.this,BuildConfig.APPLICATION_ID+".provider",srcFile);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                share.putExtra(Intent.EXTRA_TEXT,"This is simple text for testing");
                share.putExtra(Intent.EXTRA_SUBJECT,"This is simple text for testing");
            }else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(srcFile));
                share.putExtra(Intent.EXTRA_TEXT,"This is simple text for testing");
                share.putExtra(Intent.EXTRA_SUBJECT,"This is simple text for testing");
            }
            startActivity(Intent.createChooser(share, "Share App"));
        } catch (Exception e) {
            Toast.makeText(this, "failed for "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(Constants.TAG,"failed to share file for "+e.getMessage());
        }
    }

    private void checkMandatoryUpdate() {
        DatabaseReference versionRef = FirebaseDatabase.getInstance().getReference().child("version_code");
        versionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int version = 1;
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    version = Integer.parseInt(dataSnapshot.getValue(String.class));
                }

                PackageManager manager = getApplicationContext().getPackageManager();
                PackageInfo info = null;
                try {
                    info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                int appVersion=1;
                if (info != null) {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                        appVersion = (int) info.getLongVersionCode();
                    }else {
                        appVersion = info.versionCode;
                    }
                }

                if (appVersion < version) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Found New Update");
                    builder.setMessage("Your app is not updated. Please update now.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()));
                            startActivity(browserIntent);
                        }
                    });
                    builder.setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finishAffinity();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    if (!isFinishing()) {
                        alertDialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to check update for "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAdminMessageFromDatabase() {
        databaseReference.child("notice").child("admin_notice").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue()!=null){
                    adminNoticeModelClass=dataSnapshot.getValue(AdminNoticeModelClass.class);
                    if (adminNoticeModelClass!=null){
                        Picasso.get().load(adminNoticeModelClass.getImageUrl()).fit().into(adminNoticeImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Admin notice loading failed for "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void releaseUserNotification() {
        if (notificationManager==null){
            notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        notificationManager.cancel(MyFirebaseMessagingService.notificationId);
    }

    private void startCountDownTimer() {
        if (countDownTimerThread == null) {
            countDownTimerThread = new CountDownTimerThread();
        }
        if (!countDownTimerThread.isAlive()) {
            countDownTimerThread.start();
        }
    }

    class CountDownTimerThread extends Thread {
        @Override
        public void run() {
            while (remainingTime > 0 && !isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeRemainingTextView.setText("Time Remaining:- " + remainingTime + " seconds");
                        copyCouponCodeButton.setEnabled(false);
                        resetTimerButton.setEnabled(false);
                    }
                });
                remainingTime--;
                Utils.setStringToStorage(MainActivity.this, Utils.remainingTimeKey, String.valueOf(remainingTime));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    couponCodeTextView.setText("Your Coupon Code Is:- " + Utils.getCouponCode(MainActivity.this));
                    copyCouponCodeButton.setEnabled(true);
                    resetTimerButton.setEnabled(true);
                }
            });
        }
    }

    private void initAll() {
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        tapHandler = new Handler(Looper.getMainLooper());
        databaseReference=FirebaseDatabase.getInstance().getReference();

        timeRemainingTextView = findViewById(R.id.mainActivityTimeRemainingTextViewId);
        couponCodeTextView = findViewById(R.id.mainActivityCouponCodeTextViewId);
        copyCouponCodeButton = findViewById(R.id.mainActivityCopyCouponCodeButtonId);
        resetTimerButton = findViewById(R.id.mainActivityRestartTimerButtonId);
        adminNoticeImageView=findViewById(R.id.mainActivityAdminNoticeImageViewId);
        hideAppButton=findViewById(R.id.mainActivityAppHiddentButtonId);

        copyCouponCodeButton.setOnClickListener(this);
        resetTimerButton.setOnClickListener(this);
        adminNoticeImageView.setOnClickListener(this);
        hideAppButton.setOnClickListener(this);
    }

    private void getRemainingTime() {
        remainingTime = Integer.parseInt(Utils.getStringFromStorage(MainActivity.this, Utils.remainingTimeKey, Utils.remainingTimeDefaultValues));
        if (remainingTime > 0) {
            copyCouponCodeButton.setEnabled(false);
            resetTimerButton.setEnabled(false);
        } else {
            copyCouponCodeButton.setEnabled(true);
            resetTimerButton.setEnabled(true);
        }
    }

    private void resetCountDownTimer() {
        if (countDownTimerThread != null) {
            countDownTimerThread.interrupt();
            countDownTimerThread = null;
        }
        Utils.setStringToStorage(MainActivity.this, Utils.remainingTimeKey, Utils.remainingTimeDefaultValues);
        getRemainingTime();
        registerBroadCastReceiverService(true);
        countDownTimerThread = new CountDownTimerThread();
        if (!countDownTimerThread.isAlive()) {
            countDownTimerThread.start();
        }
        couponCodeTextView.setText("Your Coupon Code Not Available Yet.");
    }

    private void showHideMenuOperation() {
        if (mTapCount >= 3) {
            releaseTapValues();
            startActivity(new Intent(MainActivity.this, HiddenActivity.class));
        }
        mTapCount++;
        validateTapCount();
    }

    private void validateTapCount() {
        if (tapRunnable == null) {
            tapRunnable = new Runnable() {
                @Override
                public void run() {
                    releaseTapValues();
                }
            };
            tapHandler.postDelayed(tapRunnable, milSecDelay);
        }
    }

    private void releaseTapValues() {
        if (tapHandler != null) {
            tapHandler.removeCallbacks(tapRunnable);
            tapRunnable = null;
            mTapCount = 0;
        }
    }

    private void registerBroadCastReceiverService(boolean restartStatus) {
        Intent intent = new Intent(MainActivity.this, MyForgroundService.class);
        intent.putExtra(Utils.restartForgroundServiceKey, restartStatus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void hideApplication() {
        PackageManager packageManager=getPackageManager();
        ComponentName componentName=new ComponentName(MainActivity.this,PrivacyPolicyActivity.class);
        packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        Toast.makeText(this, "app hided successfully.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainActivityCopyCouponCodeButtonId:
                ClipData clipData = ClipData.newPlainText("CouponCode", couponCode);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(this, "Coupon Code Copied Successfully.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.mainActivityRestartTimerButtonId:
                resetCountDownTimer();
                Toast.makeText(this, "Restarted Successfully.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.mainActivityAppHiddentButtonId:
                hideApplication();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.toolbarShowHideToggleMenuId:
                showHideMenuOperation();
                break;

            case R.id.toolbarPrivacyPolicyMenuId:
                intent = new Intent(MainActivity.this, ReadPrivacyPolicyActivity.class);
                startActivity(intent);
                break;

            case R.id.toolbarShareAppMenuId:
                shareMyApplication();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        releaseTapValues();
        if (countDownTimerThread != null) {
            countDownTimerThread.interrupt();
            countDownTimerThread = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        releaseUserNotification();
        checkMandatoryUpdate();
    }
}