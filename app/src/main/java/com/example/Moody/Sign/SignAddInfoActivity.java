package com.example.Moody.Sign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignAddInfoActivity extends Activity {
    private static final String TAG = "SignActivity";
    private FirebaseAuth mAuth; //이메일,비밀번호 로그인 모듈 변수
    private FirebaseDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_info);
        mAuth = FirebaseAuth.getInstance(); //이메일 비밀번호 로그인 모듈 변수


        final EditText userName = (EditText)findViewById(R.id.sign_input_name);
        final EditText userBirth = (EditText)findViewById(R.id.sign_input_birth);
        Button backBtn = (Button)findViewById(R.id.sign_info_backBtn);
        Button okBtn = (Button)findViewById(R.id.sign_info_btn);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignAddInfoActivity.this,SignPwActivity.class));
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentData = getIntent();

                final String name = userName.getText().toString();
                final String birth = userBirth.getText().toString();
                final String email = intentData.getExtras().getString("email");
                final String password = intentData.getExtras().getString("pw");

                database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("userInfo").child(mAuth.getCurrentUser().getUid());

                HashMap<String, Object> users = new HashMap<String, Object>();
                users.put("uID",mAuth.getCurrentUser().getUid());
                users.put("email",email);
                users.put("password",password);
                users.put("name",name);
                users.put("birth",birth);
                users.put("profile","https://firebasestorage.googleapis.com/v0/b/graduation-project-ebb3e.appspot.com/o/Users%2FKakaoTalk_Photo_2020-10-31-18-19-11.png?alt=media&token=3b1cb276-6a10-47ee-a84a-126d85bf2469");
                users.put("range","all");
                users.put("ostate",true);
                users.put("lstate",true);

                myRef.setValue(users).addOnSuccessListener(new OnSuccessListener<Void>() {
                    //database에 값 전달이 성공되면
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast toast = Toast.makeText(SignAddInfoActivity.this,"가입 완료. 로그인 해주세요",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                        toast.show();
                        startActivity(new Intent(SignAddInfoActivity.this, LoginActivity.class));
                        finish();
                    }
                });



            }
        });
    }

    //키보드 내리기
    public boolean onTouchEvent(MotionEvent event) {
        EditText email = (EditText)findViewById(R.id.sign_input_name);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
        return true;
    }

}