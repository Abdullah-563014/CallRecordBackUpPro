package com.uttam.callrecord.backuppro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ReadPrivacyPolicyActivity extends AppCompatActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_privacy_policy);


        button=findViewById(R.id.privacyPolicyReadFromOnlineButtonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl("https://privacypolicy2019forandroid.blogspot.com/2020/06/privacy-policy.html");
            }
        });
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(Intent.createChooser(i,"Please select a browser"));
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}