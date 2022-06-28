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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.Moody.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignPwActivity extends Activity {
    private static final String TAG = "SignActivity";
    private FirebaseAuth mAuth; //이메일,비밀번호 로그인 모듈 변수
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_pw);
        mAuth = FirebaseAuth.getInstance();

        final EditText pw = (EditText) findViewById(R.id.sign_input_pw);
        final EditText pwCheck = (EditText) findViewById(R.id.sign_pw_check);
        final Button nextBtn = (Button) findViewById(R.id.sign_pw_nextBtn);
        final Button backBtn = (Button) findViewById(R.id.sign_pw_backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignPwActivity.this, SignActivity.class));
            }
        });


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String signPW = pw.getText().toString();
                String signPwCheck = pwCheck.getText().toString();

                if (signPW.length() < 6) {
                    Toast toast = Toast.makeText(SignPwActivity.this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                    toast.show();
                }else {
                    if (!signPW.equals(signPwCheck)) {
                        Toast toast = Toast.makeText(SignPwActivity.this, "비밀번호가 맞지 않습니다.", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                        toast.show();
                    }else {
                        String email =  getIntent().getExtras().getString("semail");
                        signUpStart(email, signPW);

                    }
                }

            }
        });

    }


    public void signUpStart(final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    HashMap<String,String> signUp = new HashMap<String,String>();
                    signUp.put("uID",mAuth.getCurrentUser().getUid());
                    signUp.put("email",email);
                    signUp.put("password",password);
                    signUp.put("name","알수없음");
                    signUp.put("birth","입력안함");
                    signUp.put("profile","");
                    database.getReference("userInfo").child(mAuth.getCurrentUser().getUid()).setValue(signUp);

                    Toast toast = Toast.makeText(SignPwActivity.this, "회원 생성 성공!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                    toast.show();

                    Intent intent = new Intent(getApplicationContext(), SignAddInfoActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("pw", password);
                    startActivity(intent);
                    finish();
                }else{
                    Toast toast = Toast.makeText(SignPwActivity.this, "회원 생성 실패!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                    toast.show();
                    Intent intent = new Intent(getApplicationContext(), SignActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    //키보드 내리기
    public boolean onTouchEvent(MotionEvent event) {
        EditText email = (EditText) findViewById(R.id.sign_input_pw);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
        return true;
    }
}