package com.uttam.callrecord.backuppro;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.services.drive.model.FileList;
import com.uttam.callrecord.backuppro.drive.DriveServiceHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class Utils {

    private static Utils utils;
    private AlertDialog alertDialog;

    public static String sharedPreferenceName="CallRecorderBackUpPro";
    public static SharedPreferences sharedPreferences;
    public static String remainingTimeKey="RemainingTimeKey";
    public static String couponCodeKey="CouponCodeKey";
    public static String recordStatusKey="RecordStatusKey";
    public static String userEmailKey="UserEmailKey";
    public static String userPasswordKey="UserPasswordKey";
    public static String loginStatusKey="LoginStatusKey";
    public static String batteryOptimizationStatusKey="BatteryOptimizationStatusKey";
    public static String remainingTimeDefaultValues="2592000";
    public static String restartForgroundServiceKey="RestartForgroundServiceKey";
    public static String privacyPolicyKey="PrivacyPolicyKey";
    public static String folderIdKey="FolderIdKey";
    public static String fcmTokenKey="FcmTokenKey";
    public static String userTypeKey="UserTypeKey";
    public static String parentReferCodeKey="ParentReferCodeKey";
    public static String myReferCodeKey="MyReferCodeKey";
    public static String userPaidStatusKey="UserPaidStatusKey";
    public static String payTimeKey="PayTimeKey";
    public static String expireTimeKey="ExpireTimeKey";




    public static void setStringToStorage(Context context, String key,String value){
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static String getStringFromStorage(Context context, String key, String defaultValue){
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(key,defaultValue);
    }

    public static void setBooleanToStorage(Context context, String key,boolean value){
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public static boolean getBooleanFromStorage(Context context, String key, boolean defaultValue){
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,Context.MODE_PRIVATE);
        }
        return sharedPreferences.getBoolean(key,defaultValue);
    }

    public static void setIntToStorage(Context context,String key, int value) {
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(key,value);
        editor.apply();
    }

    public static int getIntFromStorage(Context context, String key, int defaultValue) {
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,Context.MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key,defaultValue);
    }

    public static int getCouponCode(Context context){
        String temporaryValue=getStringFromStorage(context,couponCodeKey,null);
        if (temporaryValue==null){
            Random random=new Random();
            int randomValue= random.nextInt(99999);
            setStringToStorage(context,couponCodeKey,String.valueOf(randomValue));
            return randomValue;
        }else {
            return Integer.parseInt(temporaryValue);
        }
    }

    public static boolean haveInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return false;
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (connectivityManager.getActiveNetworkInfo() != null
                    && connectivityManager.getActiveNetworkInfo().isAvailable()
                    && connectivityManager.getActiveNetworkInfo().isConnected()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static synchronized Utils getInstance() {
        if (utils==null){
            utils=new Utils();
        }
        return utils;
    }

    public void showHideCustomAlertDialog(Context context, String message, boolean status) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog=builder.create();
        if (status){
            alertDialog.show();
        }else {
            alertDialog.dismiss();
        }
    }

    public static synchronized String getDatabasePathFromTopicOrEmail(String emailOrTopic){
        String databasePath=emailOrTopic;
        String[] symbols = {".", "#", "$", String.valueOf('['), "]"};
        String[] targetSymbols = {",", "!", "%", "*", "-"};
        for (int i = 0; i < symbols.length; i++) {
            if (databasePath.contains(symbols[i])) {
                databasePath = databasePath.replace(symbols[i], targetSymbols[i]);
            }
        }
        return databasePath;
    }

    public static synchronized String increaseTimeUsingValues(int increasingDays) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + increasingDays);
        return sdf.format(calendar.getTime());
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static String getTimeDifBetweenToTime(String startTime, String endTime) {
        try {

            Date date1;
            Date date2;

            SimpleDateFormat dates = new SimpleDateFormat("dd-MM-yyyy");

            //Setting dates
            date1 = dates.parse(startTime);
            date2 = dates.parse(endTime);

            //Comparing dates
            long difference = Math.abs(date1.getTime() - date2.getTime());
            long differenceDates = difference / (24 * 60 * 60 * 1000);

            return Long.toString(differenceDates);

        } catch (Exception exception) {
            return null;
        }
    }


}
