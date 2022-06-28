package com.example.Moody.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Moody.Activity.MainActivity;
import com.example.Moody.Feed.UploadPhotoActivity;
import com.example.Moody.R;

public class app_info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_info);

        ImageView back_btn = (ImageView) findViewById(R.id.appInfo_backBtn);
        back_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app_info.this, MainActivity.class);
                intent.putExtra("fragment","setting");
                startActivity(intent);
                finish();
            }
        });
    }
}

