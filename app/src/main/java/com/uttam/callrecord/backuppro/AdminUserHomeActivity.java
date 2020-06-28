package com.uttam.callrecord.backuppro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.uttam.callrecord.backuppro.drive.DriveServiceHelper;
import com.uttam.callrecord.backuppro.model.AdminNoticeModelClass;
import com.uttam.callrecord.backuppro.model.UserInfoModelClass;
import com.uttam.callrecord.backuppro.model.WithdrawRequestModelClass;
import com.uttam.callrecord.backuppro.service.MyFirebaseMessagingService;

import java.io.File;
import java.util.Arrays;

public class AdminUserHomeActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Button showAllCallRecordButton,shareAppButton,shareReferCodeButton,withdrawButton,getPaidVersionButton;
    private ImageView whatsAppImageView, adminNoticeImageView;
    private EditText parentReferCodeAlertDialogEditText,paymentAlertDialogPaymentMethodInfoEditText,paymentAlertDialogPaymentAmountEditText;
    private TextView paidStatusTextView, referCodeTextView,myBalanceTextView;
    private RadioGroup paymentRadioGroup;
    private AlertDialog parentReferCodeAlertDialog,paymentAlertDialog;
    private String databasePath, userEmail, topics, parentReferCode, myReferCode, userPaidStatus, payTime, expireTime, myBalance,myPaidReferCount,paymentMethod,paymentMethodInfo,paymentAmount,userName;
    private DatabaseReference databaseReference;
    private ProgressBar progressBar;
    private AdminNoticeModelClass adminNoticeModelClass;
    private NotificationManager notificationManager;
    private GoogleSignInAccount googleSignInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_home);

        initAll();

        initGoogleSignInAccount();

        getEmailAndTopicsFromLocalStorage();

        getAdminInfoFromServer();

        getAdminMessageFromDatabase();


    }


    private void initGoogleSignInAccount() {
        googleSignInAccount= GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount!=null){
            userName=googleSignInAccount.getDisplayName();
        }else {
            Toast.makeText(this, "Sorry, Unfortunately you are logout from your account. Please try to login again.", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }

    private void getEmailAndTopicsFromLocalStorage() {
        userEmail = Utils.getStringFromStorage(this, Utils.userEmailKey, null);
        if (userEmail != null) {
            String[] fragile = userEmail.split("@");
            topics = fragile[0];
        }
        myReferCode = topics;
        databasePath = topics;
        databasePath=Utils.getDatabasePathFromTopicOrEmail(databasePath);
    }

    private void initAll() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        showAllCallRecordButton = findViewById(R.id.adminUserHomeActivityShowCallRecordButtonId);
        whatsAppImageView = findViewById(R.id.adminUserHomeActivityWhatsAppImageViewId);
        adminNoticeImageView = findViewById(R.id.adminUserHomeActivityAdminNoticeImageViewId);
        paidStatusTextView = findViewById(R.id.adminUserHomeActivityPaidStatusTextViewId);
        referCodeTextView = findViewById(R.id.adminUserHomeActivityReferCodeTextViewId);
        myBalanceTextView=findViewById(R.id.adminUserHomeActivityMyBalanceTextViewId);
        shareAppButton=findViewById(R.id.adminUserHomeActivityShareAppButtonId);
        shareReferCodeButton=findViewById(R.id.adminUserHomeActivityShareReferCodeButtonId);
        withdrawButton=findViewById(R.id.adminUserHomeActivityWithdrawButtonId);
        getPaidVersionButton=findViewById(R.id.adminUserHomeActivityPayButtonId);
        progressBar = findViewById(R.id.adminUserHomeActivitySpinKitId);
        progressBar.setVisibility(View.GONE);

        showAllCallRecordButton.setOnClickListener(this);
        whatsAppImageView.setOnClickListener(this);
        adminNoticeImageView.setOnClickListener(this);
        shareAppButton.setOnClickListener(this);
        shareReferCodeButton.setOnClickListener(this);
        withdrawButton.setOnClickListener(this);
        getPaidVersionButton.setOnClickListener(this);
    }

    private void checkMandatoryUpdate() {
        databaseReference.child("version_code").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AdminUserHomeActivity.this);
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
                Toast.makeText(AdminUserHomeActivity.this, "Failed to check update for "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            Toast.makeText(AdminUserHomeActivity.this, "WhatsApp app not installed in your phone", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(Intent.createChooser(i,"Please select a browser"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void shareReferCode() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        String shareBody = "Refer Code Is:- "+myReferCode;
        String shareSubject="Please use this refer code to get ₹50 bonus";
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Please choose on of them."));
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
            share.setType("application/vnd.android.package-archive");
            if (Build.VERSION.SDK_INT>=24){
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri fileUri= FileProvider.getUriForFile(AdminUserHomeActivity.this, BuildConfig.APPLICATION_ID+".provider",srcFile);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
            }else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(srcFile));
            }
            startActivity(Intent.createChooser(share, "Share App"));
        } catch (Exception e) {
            Toast.makeText(this, "failed for "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(Constants.TAG,"failed to share app for "+e.getMessage());
        }
    }

    private void releaseUserNotification() {
        if (notificationManager==null){
            notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        notificationManager.cancel(MyFirebaseMessagingService.notificationId);
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
                Toast.makeText(AdminUserHomeActivity.this, "Admin notice loading failed for "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserAsAdminUser() {
        Utils.setBooleanToStorage(AdminUserHomeActivity.this, Utils.recordStatusKey, false);
    }

    private void updateUserInfoToTextView() {
        if (userPaidStatus.equalsIgnoreCase("false")) {
            paidStatusTextView.setText("User Type:- Free");
        } else {
            paidStatusTextView.setText("User Type:- Premium");
        }
        myBalanceTextView.setText("Your Balance Is:- "+myBalance);
        referCodeTextView.setText("Your Refer Code:- " + myReferCode);
    }

    private void showParentReferCodeAlertDialog() {
        View view = getLayoutInflater().inflate(R.layout.parent_refer_code_alert_dialog_custom_view, null, false);
        Button confirmButton = view.findViewById(R.id.parentReferCodeAlertDialogConfirmButtonId);
        Button cancelButton = view.findViewById(R.id.parentReferCodeAlertDialogCancelButtonId);
        parentReferCodeAlertDialogEditText = view.findViewById(R.id.parentReferCodeAlertDialogEditTextId);
        confirmButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminUserHomeActivity.this)
                .setCancelable(false)
                .setView(view);
        parentReferCodeAlertDialog = builder.create();
        if (!isFinishing()) {
            parentReferCodeAlertDialog.show();
        }
    }

    private void saveAdminInfoToServer() {
        if (userEmail != null && !TextUtils.isEmpty(userEmail) && topics != null && !TextUtils.isEmpty(topics) && parentReferCode != null && !TextUtils.isEmpty(parentReferCode) && myReferCode != null && !TextUtils.isEmpty(myReferCode) && userPaidStatus != null && !TextUtils.isEmpty(userPaidStatus) && payTime != null && !TextUtils.isEmpty(payTime) && expireTime != null && !TextUtils.isEmpty(expireTime) && myBalance != null && !TextUtils.isEmpty(myBalance) && myPaidReferCount != null && !TextUtils.isEmpty(myPaidReferCount)) {
            progressBar.setVisibility(View.VISIBLE);
            UserInfoModelClass userInfoModelClass = new UserInfoModelClass(userEmail, topics, parentReferCode, myReferCode, userPaidStatus, payTime, expireTime, myBalance,myPaidReferCount);
            databaseReference.child("Users").child(databasePath).setValue(userInfoModelClass)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Utils.setStringToStorage(AdminUserHomeActivity.this, Utils.parentReferCodeKey, parentReferCode);
                            Utils.setStringToStorage(AdminUserHomeActivity.this, Utils.userPaidStatusKey, userPaidStatus);
                            updateUserInfoToTextView();
                            Toast.makeText(AdminUserHomeActivity.this, "Your info updated successfully.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminUserHomeActivity.this, "Failed to save your info to database for " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "Yet, some information are empty.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getAdminInfoFromServer() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.child("Users").child(databasePath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    UserInfoModelClass modelClass = dataSnapshot.getValue(UserInfoModelClass.class);
                    if (modelClass != null) {
                        userEmail = modelClass.getEmail();
                        topics = modelClass.getTopics();
                        parentReferCode = modelClass.getParentReferCode();
                        myReferCode = modelClass.getMyReferCode();
                        userPaidStatus = modelClass.getPaidStatus();
                        payTime = modelClass.getPayTime();
                        expireTime = modelClass.getExpireTime();
                        myBalance = modelClass.getMyBalance();
                        myPaidReferCount = modelClass.getMyPaidReferCount();
                    }
                    updateUserInfoToTextView();
                } else {
                    showParentReferCodeAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    private void showPaymentAlertDialog() {
        View view=getLayoutInflater().inflate(R.layout.payment_alert_dialog_custom_layout,null,false);
        Button cancelButton=view.findViewById(R.id.paymentAlertDialogCancelButtonId);
        Button confirmButton=view.findViewById(R.id.paymentAlertDialogConfirmButtonId);
        paymentAlertDialogPaymentMethodInfoEditText=view.findViewById(R.id.paymentAlertDialogPaymentMethodInfoEditTextId);
        paymentAlertDialogPaymentAmountEditText=view.findViewById(R.id.paymentAlertDialogPaymentAmountEditTextId);
        paymentRadioGroup=view.findViewById(R.id.paymentAlertDialogRadioGroupId);

        paymentRadioGroup.setOnCheckedChangeListener(this);
        cancelButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);

        AlertDialog.Builder builder=new AlertDialog.Builder(AdminUserHomeActivity.this)
                .setCancelable(false)
                .setView(view);
        paymentAlertDialog=builder.create();
        if (!isFinishing()){
            paymentAlertDialog.show();
        }
    }

    private void startPaymentRequest() {
        paymentMethodInfo=paymentAlertDialogPaymentMethodInfoEditText.getText().toString();
        paymentAmount=paymentAlertDialogPaymentAmountEditText.getText().toString();
        if (!TextUtils.isEmpty(paymentAmount) && !TextUtils.isEmpty(paymentMethodInfo) && paymentMethod!=null && !TextUtils.isEmpty(paymentMethod)){
            int withdrawRupee=Integer.parseInt(paymentAmount);
            if (withdrawRupee<=Integer.parseInt(myBalance)){
                if (withdrawRupee>=100){
                    progressBar.setVisibility(View.VISIBLE);
                    WithdrawRequestModelClass requestModelClass=new WithdrawRequestModelClass(userEmail,userName,paymentAmount,paymentMethod,paymentMethodInfo);
                    databaseReference.child("WithdrawRequest").child(databasePath).setValue(requestModelClass)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminUserHomeActivity.this, "Payment request sent successfully.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminUserHomeActivity.this, "Failed to send payment request for "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(this, "Sorry, Minimum withdraw amount is ₹100.", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "Your withdraw amount is greater than your original balance.", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Please input all information properly and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMyBalance(String amount) {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.child("Users").child(databasePath).child("myBalance")
                .setValue(String.valueOf((Integer.parseInt(myBalance)+Integer.parseInt(amount))))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AdminUserHomeActivity.this, "Successfully you got "+amount+" bonus.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AdminUserHomeActivity.this, "Bonus not added in your account for "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateParentBalance(String amount) {
        if (parentReferCode!=null){
            progressBar.setVisibility(View.VISIBLE);
            databaseReference.child("Users").child(Utils.getDatabasePathFromTopicOrEmail(parentReferCode)).child("myBalance")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getValue()!=null){
                                String oldBalance=dataSnapshot.getValue(String.class);
                                if (oldBalance!=null){
                                    databaseReference.child("Users").child(Utils.getDatabasePathFromTopicOrEmail(parentReferCode)).child("myBalance")
                                            .setValue(String.valueOf((Integer.parseInt(oldBalance)+Integer.parseInt(amount))))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(AdminUserHomeActivity.this, "Successfully your referer got "+amount+" bonus.", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(AdminUserHomeActivity.this, "Bonus not added in your referer account for "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }else {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AdminUserHomeActivity.this, "Your referer balance info not available in database.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminUserHomeActivity.this, "Failed to add bonus for "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            Toast.makeText(this, "Failed for referer code empty.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setUserAsAdminUser();
        checkMandatoryUpdate();
        releaseUserNotification();

        if (topics != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(topics);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.parentReferCodeAlertDialogCancelButtonId:
                parentReferCode = "0000";
                userPaidStatus = "false";
                payTime = "0";
                expireTime = "0";
                myBalance = "0";
                myPaidReferCount = "0";
                saveAdminInfoToServer();
                parentReferCodeAlertDialog.dismiss();
                break;

            case R.id.parentReferCodeAlertDialogConfirmButtonId:
                parentReferCode = parentReferCodeAlertDialogEditText.getText().toString();
                userPaidStatus = "false";
                payTime = "0";
                expireTime = "0";
                myBalance = "0";
                myPaidReferCount = "0";
                saveAdminInfoToServer();
                parentReferCodeAlertDialog.dismiss();
                break;

            case R.id.adminUserHomeActivityWhatsAppImageViewId:
                openWhatsApp();
                break;

            case R.id.adminUserHomeActivityShareReferCodeButtonId:
                shareReferCode();
                break;

            case R.id.adminUserHomeActivityShareAppButtonId:
                shareMyApplication();
                break;

            case R.id.adminUserHomeActivityAdminNoticeImageViewId:
                if (adminNoticeModelClass!=null && adminNoticeModelClass.getTargetUrl()!=null){
                    openUrl(adminNoticeModelClass.getTargetUrl());
                }
                break;

            case R.id.adminUserHomeActivityWithdrawButtonId:
                showPaymentAlertDialog();
                break;

            case R.id.paymentAlertDialogCancelButtonId:
                paymentAlertDialog.dismiss();
                break;

            case R.id.paymentAlertDialogConfirmButtonId:
                startPaymentRequest();
                break;

            case R.id.adminUserHomeActivityPayButtonId:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int checkRadioButtonId=group.getCheckedRadioButtonId();
        if (checkRadioButtonId==R.id.paymentAlertDialogPhonePayRadioButtonId){
            paymentMethod="Phone Pay";
        }else if (checkRadioButtonId==R.id.paymentAlertDialogPayTmRadioButtonId){
            paymentMethod="PayTm";
        }else if (checkRadioButtonId==R.id.paymentAlertDialogGooglePayRadioButtonId){
            paymentMethod="Google Pay";
        }else if (checkRadioButtonId==R.id.paymentAlertDialogUpiRadioButtonId){
            paymentMethod="Upi";
        }
        Log.d(Constants.TAG,"payment method is "+paymentMethod);
    }


}