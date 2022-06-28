package com.example.Moody.Feed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.Moody.R;

public class DetailPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //배경 투명하게
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.image_detail_popup);
        TextView result=(TextView)findViewById(R.id.result_text);
        ImageView image=(ImageView)findViewById(R.id.popup_image);

        //데이터 가져오기
        Intent intent = getIntent();
        if(intent.getStringExtra("url")!=null) {
            String data = intent.getStringExtra("res");
            String url = intent.getStringExtra("url");
            System.out.println("res:" + data);
            System.out.println("url:" + url);
            result.setText(data);
            Glide.with(DetailPopupActivity.this).load(url).into(image);
        }
        else{
            String data=intent.getStringExtra("result");
            byte[] imgByte=intent.getByteArrayExtra("image");
            result.setText(data);
            Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByte,0, imgByte.length );
            image.setImageBitmap(imgBitmap);


        }
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}