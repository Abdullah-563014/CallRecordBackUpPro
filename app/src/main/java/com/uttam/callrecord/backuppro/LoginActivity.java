package com.uttam.callrecord.backuppro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.uttam.callrecord.backuppro.drive.DriveServiceHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private GoogleSignInAccount googleSignInAccount;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private int RC_SIGN_IN = 10010;
    private int PERMISSION_ALL = 1;
    private DriveServiceHelper driveServiceHelper;
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE),new Scope(DriveScopes.DRIVE_METADATA))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.READ_CALL_LOG
            };

            if (!hasPermission(LoginActivity.this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
            } else {
                startEveryThing();
            }
        } else {
            startEveryThing();
        }
    }

    private void startEveryThing() {
        initAll();
    }

    private void initAll() {
        loginButton = findViewById(R.id.loginActivityLoginButtonId);

        loginButton.setOnClickListener(this);
    }

    private void startLoginOperation() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(LoginActivity.this, Arrays.asList(DriveScopes.DRIVE_FILE,DriveScopes.DRIVE_METADATA));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());
                        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential)
                                .setApplicationName(getResources().getString(R.string.app_name))
                                .build();
                        driveServiceHelper = new DriveServiceHelper(googleDriveService,LoginActivity.this);
                        if (!GoogleSignIn.hasPermissions(googleSignInAccount, new Scope(DriveScopes.DRIVE_FILE),new Scope(DriveScopes.DRIVE_METADATA))) {
                            GoogleSignIn.requestPermissions(LoginActivity.this, RC_SIGN_IN, googleSignInAccount, new Scope(DriveScopes.DRIVE_FILE),new Scope(DriveScopes.DRIVE_METADATA));
                        } else {
                            if (Utils.getStringFromStorage(LoginActivity.this,Utils.folderIdKey,null)==null){
                                driveServiceHelper.createFolder();
                            }
                        }
                        Utils.setStringToStorage(LoginActivity.this,Utils.userEmailKey,googleSignInAccount.getEmail());
                        startActivity(new Intent(LoginActivity.this,UserTypeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Failed for "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean hasPermission(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.loginActivityLoginButtonId:
                startLoginOperation();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleSignInAccount=GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if (googleSignInAccount!=null && Utils.getStringFromStorage(LoginActivity.this,Utils.folderIdKey,null)!=null && GoogleSignIn.hasPermissions(googleSignInAccount, new Scope(DriveScopes.DRIVE_FILE),new Scope(DriveScopes.DRIVE_METADATA))){
            startActivity(new Intent(LoginActivity.this,UserTypeActivity.class));
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK && data != null) {
            handleSignInResult(data);
        } else {
            Toast.makeText(this, "Please accept all permission to use this application.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ALL) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need to grant all permission to run this app", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    startEveryThing();
                }
            }
        }
    }
}