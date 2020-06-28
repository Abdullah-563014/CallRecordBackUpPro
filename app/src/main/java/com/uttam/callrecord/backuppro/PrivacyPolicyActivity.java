package com.uttam.callrecord.backuppro;

import androidx.appcompat.app.AppCompatActivity;

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
                    startActivity(new Intent(PrivacyPolicyActivity.this,LoginActivity.class));
                    finish();
                }else {
                    Toast.makeText(PrivacyPolicyActivity.this, "Please Accept our Privacy Policy to continue this app.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initAll() {
        checkBox=findViewById(R.id.privacyPolicyCheckBoxId);
        acceptButton=findViewById(R.id.privacyPolicyAcceptButtonId);
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