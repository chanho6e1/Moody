package com.example.Moody.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Moody.Firebase.Image;
import com.example.Moody.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class IntroActivity extends AppCompatActivity {
    Animation mAnim1;
    Animation mAnim2;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static HashMap<Integer, String> word_set;
    Handler mHandler;
    Intent intent;
    public static ArrayList<Image> publicItems = new ArrayList<Image>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnim1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_anim);
        mAnim1.setInterpolator(getApplicationContext(), android.R.anim.accelerate_interpolator);
        mAnim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.moody_fade_in_anim);
        mAnim2.setInterpolator(getApplicationContext(), android.R.anim.accelerate_interpolator);

        //상태바 없애기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }


        setContentView(R.layout.intro); //intro.xml과 연결

        word_set = Word(); //단어집합 가져오기
        getImageList();

        //화면 클릭 시 애니메이션 작동
        Button window = (Button)findViewById(R.id.button4);
        final ImageView logo = (ImageView)findViewById(R.id.logo);
        final ImageView moody = (ImageView)findViewById(R.id.moody);

        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                //moody 보이기
                moody.setVisibility(v.VISIBLE);
                moody.startAnimation(mAnim2);
                //logo 축소
                logo.startAnimation(mAnim1);
                //딜레이
                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                        startActivity(intent);
                        //점점 사라지기
                        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
                    }
                }, 600);

            }
        });
    }

    public void FirebaseData(){

    }





    /*Runnable run = new Runnable() {
        @Override
        public void run() {
            //다음화면으로 넘어가기 handler
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            //finish(); // activity화면 제거
        }
    };
    @Override
    protected void onResume() {
        super.onResume(); //handler에 예약 걸기
        handler.postDelayed(run,1000); //1초뒤에 Runnable() 객체 실행
    }

    @Override
    protected void onPause() {
        super.onPause(); //화면을 벗어나면, handler에 예약한 작업 취소
        handler.removeCallbacks(run); //예약취소
    }*/

    //단어집합 가져오기
    private HashMap<Integer, String> Word() {

        HashMap<Integer, String> word_set = new HashMap<Integer, String>();

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.wordset);
            InputStreamReader reader = new InputStreamReader(inputStream);
            // 입력 버퍼 생성
            BufferedReader bufReader = new BufferedReader(reader);
            String line = "";

            int i=0;
            while ((line = bufReader.readLine()) != null) {
                String[] word = line.split(":");
                word_set.put(Integer.parseInt(word[1]), word[0]);
                i++;
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return word_set;
    }

    public static <K, V> K getKey(HashMap<K, V> map, V value) {

        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }

    //공용이미지 가져오기
    private void getImageList() {
        final ProgressDialog mProgressDialog = new ProgressDialog(IntroActivity.this);
        DatabaseReference databaseReference;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Image");
        //mProgressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressDialog.dismiss();
                publicItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Image image = snapshot.getValue(Image.class);
                    publicItems.add(image);

                }
                //FragmentFeed.adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });
    }
}
