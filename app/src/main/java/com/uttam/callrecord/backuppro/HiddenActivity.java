package com.uttam.callrecord.backuppro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class HiddenActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button allCallRecord,openApplicationSettingButton,autoStartOnOffButton;
    private SwitchCompat recordOnOffSwitch;
    private TextView userTypeTextView,batteryOptimizationStatusTextView,backgroundDataUsingRestrictionStatusTextView;
    private ImageView whatsAppImageView;
    private boolean recordStatus;
    private String packageName;
    private PowerManager pm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden);


        initAll();


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

    private void getRecordSwitchStatus() {
        recordStatus=Utils.getBooleanFromStorage(HiddenActivity.this,Utils.recordStatusKey,false);
        if (Constants.isDisableBatteryOptimization && Constants.isBackgroundDataRestrictionDisable){
            recordOnOffSwitch.setChecked(recordStatus);
        }else {
            recordOnOffSwitch.setChecked(false);
            recordStatus=false;
            Utils.setBooleanToStorage(HiddenActivity.this,Utils.recordStatusKey,false);
        }
        setUserTypeToTextView();
    }

    private void initAll() {
        packageName=getPackageName();
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        allCallRecord=findViewById(R.id.hiddenActivityAllCallRecordButtonId);
        recordOnOffSwitch=findViewById(R.id.hiddenActivityRecordOnOffSwitchId);
        batteryOptimizationStatusTextView=findViewById(R.id.hiddenActivityBatteryOptimizationStatusTextViewId);
        backgroundDataUsingRestrictionStatusTextView=findViewById(R.id.hiddenActivityRestrictBackgroundDataUsingStatusTextViewId);
        userTypeTextView=findViewById(R.id.hiddenActivityUserTypeTextViewId);
        openApplicationSettingButton=findViewById(R.id.hiddenActivityOpenApplicationSettingButtonId);
        autoStartOnOffButton=findViewById(R.id.hiddenActivityOpenAutoStartOnOffSettingButtonId);
        whatsAppImageView=findViewById(R.id.hiddenActivityWhatsAppImageViewId);

        recordOnOffSwitch.setOnCheckedChangeListener(this);
        allCallRecord.setOnClickListener(this);
        openApplicationSettingButton.setOnClickListener(this);
        autoStartOnOffButton.setOnClickListener(this);
        whatsAppImageView.setOnClickListener(this);
    }

    private void setUserTypeToTextView() {
        if (recordStatus){
            userTypeTextView.setText("Backup On");
            if (Build.VERSION.SDK_INT>=23){
                userTypeTextView.setTextColor(ContextCompat.getColor(HiddenActivity.this,R.color.colorAccent));
            }else {
                userTypeTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        }else {
            userTypeTextView.setText("Backup Off");
            if (Build.VERSION.SDK_INT>=23){
                userTypeTextView.setTextColor(ContextCompat.getColor(HiddenActivity.this,R.color.colorBlack));
            }else {
                userTypeTextView.setTextColor(getResources().getColor(R.color.colorBlack));
            }
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

    private void openWhatsApp() {
        String contact = "+918585807175";
        String url = "https://api.whatsapp.com/send?phone=" + contact;
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(HiddenActivity.this, "WhatsApp app not installed in your phone", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.hiddenActivityAllCallRecordButtonId:
                intent=new Intent(HiddenActivity.this,HomeActivity.class);
                startActivity(intent);
                break;

            case R.id.hiddenActivityOpenApplicationSettingButtonId:
                try {
                    intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(this, "Failed for "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.hiddenActivityOpenAutoStartOnOffSettingButtonId:
                openAutoStartOnOffSettingPage();
                break;

            case R.id.hiddenActivityWhatsAppImageViewId:
                openWhatsApp();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.hiddenActivityRecordOnOffSwitchId:
                Utils.setBooleanToStorage(HiddenActivity.this,Utils.recordStatusKey,isChecked);
                recordOnOffSwitch.setChecked(isChecked);
                recordStatus=isChecked;
                setUserTypeToTextView();
                if (isChecked){
                    if (Build.VERSION.SDK_INT>=23){
                        if (!Constants.isDisableBatteryOptimization || !Constants.isBackgroundDataRestrictionDisable){
                            Toast.makeText(this, "Please disable battery optimization and background data restriction first and try again.", Toast.LENGTH_LONG).show();
                            Utils.setBooleanToStorage(HiddenActivity.this,Utils.recordStatusKey,false);
                            recordOnOffSwitch.setChecked(false);
                            recordStatus=false;
                            setUserTypeToTextView();
                        }
                    }
                    allCallRecord.setEnabled(false);
                }else {
                    allCallRecord.setEnabled(true);
                }
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
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getBatteryOptimizationStatus();
        getBackgroundDataUsingRestrictionStatus();
        getRecordSwitchStatus();
//        getBackgroundServiceRestrictionStatus();

        String userEmail=Utils.getStringFromStorage(this,Utils.userEmailKey,null);
        if (userEmail!=null){
            if (recordStatus){
                FirebaseMessaging.getInstance().unsubscribeFromTopic(userEmail);
            }else {
                FirebaseMessaging.getInstance().subscribeToTopic(userEmail);
            }
        }

        if (recordStatus){
            allCallRecord.setEnabled(false);
        }else {
            allCallRecord.setEnabled(true);
        }
    }
}