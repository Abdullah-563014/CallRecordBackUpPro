package com.uttam.callrecord.backuppro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private CheckBox checkBox;
    private Button acceptButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);


        initAll();


        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()){
                    Utils.setBooleanToStorage(getApplicationContext(),Utils.privacyPolicyKey,true);
                    if (Utils.haveInternet(PrivacyPolicyActivity.this)){
                        gotoLoginActivity();
                    }else {
                        noInternetAlertDialog();
                    }
                }else {
                    Toast.makeText(PrivacyPolicyActivity.this, "Please Accept our Privacy Policy to continue this app.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void gotoLoginActivity() {
        startActivity(new Intent(PrivacyPolicyActivity.this,LoginActivity.class));
        finish();
    }

    private void initAll() {
        checkBox=findViewById(R.id.privacyPolicyCheckBoxId);
        acceptButton=findViewById(R.id.privacyPolicyAcceptButtonId);
    }

    private void noInternetAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrivacyPolicyActivity.this)
                .setMessage("No internet connection. Please check your internet connect and try again.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        if (!isFinishing()){
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.getBooleanFromStorage(PrivacyPolicyActivity.this,Utils.privacyPolicyKey,false)){
            startActivity(new Intent(PrivacyPolicyActivity.this,LoginActivity.class));
            finish();
        }
    }
}