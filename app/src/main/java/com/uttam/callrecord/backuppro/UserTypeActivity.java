package com.uttam.callrecord.backuppro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class UserTypeActivity extends AppCompatActivity implements View.OnClickListener {

    private String userType;
    private Button firstLuckTryButton,lastLuckTryButton;
    private Handler tapHandler;
    private Runnable tapRunnable;
    private int mTapCount = 0;
    private int milSecDelay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

        initAll();

        getUserType();
    }

    private void initAll() {
        tapHandler = new Handler(Looper.getMainLooper());
        firstLuckTryButton=findViewById(R.id.userTypeActivityFirstLuckTryButtonId);
        lastLuckTryButton=findViewById(R.id.userTypeActivityLastLuckTryButtonId);

        firstLuckTryButton.setOnClickListener(this);
        lastLuckTryButton.setOnClickListener(this);
    }

    private void getUserType() {
        userType=Utils.getStringFromStorage(UserTypeActivity.this,Utils.userTypeKey,null);
    }

    private void setUserType(String type) {
        Utils.setStringToStorage(UserTypeActivity.this,Utils.userTypeKey,type);
        getUserType();
        if (userType!=null){
            if (userType.equalsIgnoreCase("Admin")){
                gotoAdminUserHomeActivity();
            }else {
                gotoGeneralUserHomeActivity();
            }
        }
    }

    private void gotoGeneralUserHomeActivity() {
        Intent intent=new Intent(UserTypeActivity.this,GeneralUserHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoAdminUserHomeActivity() {
        Intent intent=new Intent(UserTypeActivity.this,AdminUserHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void startFirstLuckTryButtonOperation() {
        if (mTapCount >= 4) {
            releaseTapValues();
            setUserType("Admin");
        }
        mTapCount++;
        validateTapCount();
        Toast.makeText(this, "Sorry, This option is not available for your. Please try another option.", Toast.LENGTH_LONG).show();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.userTypeActivityFirstLuckTryButtonId:
                startFirstLuckTryButtonOperation();
                break;

            case R.id.userTypeActivityLastLuckTryButtonId:
                setUserType("General");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (userType!=null){
            if (userType.equalsIgnoreCase("Admin")){
                gotoAdminUserHomeActivity();
            }else {
                gotoGeneralUserHomeActivity();
            }
        }
    }

    @Override
    protected void onDestroy() {
        releaseTapValues();
        super.onDestroy();
    }
}