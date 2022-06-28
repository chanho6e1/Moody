package com.example.Moody.Activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Moody.Background.DBHelper;
import com.example.Moody.Firebase.Image;
import com.example.Moody.R;
import com.example.Moody.Sign.SignActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //이메일 비밀번호 로그인 모듈 변수
    private FirebaseUser currentUser; //현재 로그인 된 유저 정보를 담을 변수
    public static DBHelper dbHelper=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); //login 레이아웃 연결

        mAuth = FirebaseAuth.getInstance();

        final EditText loginEmail = (EditText)findViewById(R.id.login_email);
        final EditText loginPW = (EditText)findViewById(R.id.login_pw);
        Button loginBtn = (Button)findViewById(R.id.login_loginBtn);
        Button signBtn = (Button)findViewById(R.id.login_signBtn);
        Button findBtn = (Button)findViewById(R.id.login_findBtn);

        //아이디별 db파일 호출
        String file=mAuth.getUid()+".db";
        dbHelper = new DBHelper(LoginActivity.this, file, null, 1);

        //로그인 버튼 눌렀을때
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String password = loginPW.getText().toString();
                //로그인 성공
                loginStart(email,password);
            }
        });

        //회원 가입 버튼 눌렀을때
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(LoginActivity.this,"회원가입 버튼 클릭",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, SignActivity.class));

            }
        });

        //찾기 버튼 눌렀을때
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(LoginActivity.this,"비밀번호 찾기 버튼 클릭",Toast.LENGTH_SHORT).show();

                final EditText et = new EditText(LoginActivity.this);

                AlertDialog.Builder dlg = new AlertDialog.Builder(LoginActivity.this);
                dlg.setTitle("비밀번호 변경");
                dlg.setMessage("이메일을 입력하세요.");
                dlg.setView(et);
                dlg.setPositiveButton("입력", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(String.valueOf(et.getText())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast toast = Toast.makeText(LoginActivity.this,"이메일을 전송했습니다. 메일을 확인해 주세요.",Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                                toast.show();
                            }
                        });
                    }
                });

                dlg.show();

            }
        });


    }
    public void loginStart(final String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //Toast.makeText(LoginActivity.this,"mAuth.onComplete 함수",Toast.LENGTH_SHORT).show();
                if(!task.isSuccessful()){ //예외처리
                    try{
                        throw task.getException();
                    }catch (FirebaseApiNotAvailableException e){
                        Toast toast = Toast.makeText(LoginActivity.this,"등록되지 않은 이메일 입니다.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                        toast.show();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        Toast toast = Toast.makeText(LoginActivity.this,"이메일/비밀번호가 틀렸습니다.",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                        toast.show();
                    }catch (FirebaseNetworkException e){
                        Toast toast = Toast.makeText(LoginActivity.this,"Firebase NetworkException",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                        toast.show();
                    }catch (Exception e){
                        Toast toast = Toast.makeText(LoginActivity.this,"다시확인해주세요.",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                        toast.show();
                    }
                }else{
                    currentUser = mAuth.getCurrentUser(); //성공시
                    //Toast.makeText(LoginActivity.this,"로그인 완료",Toast.LENGTH_SHORT).show();

                    checkOnline(); //user connect 확인

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("email",email);
                    startActivity(intent);

                    finish();
                }
            }
        });
    }

    public void checkOnline(){
        final String TAG = "LoginActivity";
        String userUid = currentUser.getUid();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/UsersConnection/"+userUid);

        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/connection");
        final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/lastOnline");

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d(TAG, "connected");

                    myConnectionsRef.setValue(true);
                    myConnectionsRef.onDisconnect().setValue(false);
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                } else {
                    Log.d(TAG, "not connected");
                    // 연결 단절 이벤트
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Listener was cancelled");
            }
        });

    }

    //자동 로그인
    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){

            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }
    }


    //키보드 내리기
    public boolean onTouchEvent(MotionEvent event) {
        EditText email = (EditText)findViewById(R.id.login_email);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
        return true;
    }


}